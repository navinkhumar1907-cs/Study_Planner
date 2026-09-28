/**
 * Study Planner - Main Frontend JavaScript
 * Handles REST API communication, state management, form validations,
 * timetable generation, and progress tracking.
 */

// Base API URL: dynamically detect origin or fallback to localhost:8080
const API_BASE = (window.location.protocol === 'http:' || window.location.protocol === 'https:') && window.location.port === '8080'
    ? `${window.location.origin}/api`
    : 'http://localhost:8080/api';

// ==========================================
// Authentication & Session Management
// ==========================================
function getCurrentUser() {
    try {
        const userJson = localStorage.getItem('studyPlannerUser');
        return userJson ? JSON.parse(userJson) : null;
    } catch (e) {
        return null;
    }
}

function setCurrentUser(user) {
    localStorage.setItem('studyPlannerUser', JSON.stringify(user));
}

function clearCurrentUser() {
    localStorage.removeItem('studyPlannerUser');
}

function requireAuth() {
    const user = getCurrentUser();
    if (!user || !user.userId) {
        window.location.href = 'login.html';
        return null;
    }
    return user;
}

function setupNavbar() {
    const user = getCurrentUser();
    const navUserContainer = document.getElementById('navUserContainer');
    const navLinks = document.getElementById('navLinks');

    if (!navUserContainer) return;

    if (user && user.userId) {
        navUserContainer.innerHTML = `
            <span class="user-badge">👤 ${escapeHtml(user.name || user.email)}</span>
            <button class="btn btn-sm btn-secondary" onclick="logoutUser()">Log Out</button>
        `;
    } else {
        navUserContainer.innerHTML = `
            <a href="login.html" class="btn btn-sm btn-secondary">Log In</a>
            <a href="register.html" class="btn btn-sm btn-primary">Sign Up</a>
        `;
    }
}

function logoutUser() {
    clearCurrentUser();
    window.location.href = 'login.html';
}

function showAlert(containerId, message, type = 'error') {
    const container = document.getElementById(containerId);
    if (!container) return;

    const alertClass = type === 'success' ? 'alert-success' : (type === 'info' ? 'alert-info' : 'alert-error');
    const icon = type === 'success' ? '✓' : (type === 'info' ? 'ℹ' : '⚠');

    container.innerHTML = `
        <div class="alert ${alertClass}">
            <span>${icon}</span>
            <span>${escapeHtml(message)}</span>
        </div>
    `;
    container.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function clearAlert(containerId) {
    const container = document.getElementById(containerId);
    if (container) container.innerHTML = '';
}

function escapeHtml(text) {
    if (!text) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// ==========================================
// REGISTER PAGE LOGIC
// ==========================================
function initRegisterPage() {
    const form = document.getElementById('registerForm');
    if (!form) return;

    // If already logged in, redirect to dashboard
    if (getCurrentUser()) {
        window.location.href = 'dashboard.html';
        return;
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAlert('alertBox');

        const name = document.getElementById('name').value.trim();
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        // Validations
        if (!name) {
            showAlert('alertBox', 'Please enter your full name.');
            return;
        }
        if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            showAlert('alertBox', 'Please enter a valid email address.');
            return;
        }
        if (!password || password.length < 4) {
            showAlert('alertBox', 'Password must be at least 4 characters long.');
            return;
        }
        if (password !== confirmPassword) {
            showAlert('alertBox', 'Password and confirm password do not match.');
            return;
        }

        const submitBtn = document.getElementById('submitBtn');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Creating Account...';

        try {
            const res = await fetch(`${API_BASE}/users/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email, password, confirmPassword })
            });

            const data = await res.json();

            if (!res.ok || !data.success) {
                showAlert('alertBox', data.message || 'Registration failed. Please try again.');
            } else {
                showAlert('alertBox', 'Account created successfully! Redirecting to login...', 'success');
                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 1500);
            }
        } catch (err) {
            showAlert('alertBox', 'Unable to connect to backend server. Make sure Spring Boot is running on port 8080.');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Register';
        }
    });
}

// ==========================================
// LOGIN PAGE LOGIC
// ==========================================
function initLoginPage() {
    const form = document.getElementById('loginForm');
    if (!form) return;

    if (getCurrentUser()) {
        window.location.href = 'dashboard.html';
        return;
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAlert('alertBox');

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;

        if (!email || !password) {
            showAlert('alertBox', 'Please enter both email and password.');
            return;
        }

        const submitBtn = document.getElementById('submitBtn');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Logging In...';

        try {
            const res = await fetch(`${API_BASE}/users/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            const data = await res.json();

            if (!res.ok || !data.success) {
                showAlert('alertBox', data.message || 'Invalid email or password.');
            } else {
                setCurrentUser({
                    userId: data.userId,
                    name: data.name,
                    email: data.email
                });
                window.location.href = 'dashboard.html';
            }
        } catch (err) {
            showAlert('alertBox', 'Unable to reach backend server. Please verify Spring Boot is running on port 8080.');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Log In';
        }
    });
}

// ==========================================
// DASHBOARD PAGE LOGIC
// ==========================================
async function initDashboardPage() {
    const user = requireAuth();
    if (!user) return;

    const welcomeName = document.getElementById('welcomeName');
    if (welcomeName) welcomeName.textContent = user.name || user.email;

    await loadDashboardStats(user.userId);
}

async function loadDashboardStats(userId) {
    try {
        const [progressRes, subjectsRes] = await Promise.all([
            fetch(`${API_BASE}/schedules/progress/${userId}`),
            fetch(`${API_BASE}/subjects/user/${userId}`)
        ]);

        if (progressRes.ok) {
            const progress = await progressRes.json();
            document.getElementById('statTotalSubjects').textContent = progress.totalSubjects;
            document.getElementById('statTotalHours').textContent = `${progress.totalPlannedHours}h`;
            document.getElementById('statCompletedSessions').textContent = progress.completedSessions;
            document.getElementById('statPendingSessions').textContent = progress.pendingSessions;
            
            const pct = Math.round(progress.completionPercentage || 0);
            document.getElementById('progressPercentageText').textContent = `${pct}%`;
            document.getElementById('progressBarFill').style.width = `${pct}%`;
            document.getElementById('dashTotalSessions').textContent = progress.totalSessions;
            document.getElementById('dashCompletedSessions').textContent = progress.completedSessions;
            document.getElementById('dashPendingSessions').textContent = progress.pendingSessions;
        }

        if (subjectsRes.ok) {
            const subjects = await subjectsRes.json();
            const subjectContainer = document.getElementById('dashboardSubjectList');
            if (subjectContainer) {
                if (subjects.length === 0) {
                    subjectContainer.innerHTML = `
                        <div style="text-align: center; color: var(--text-muted); padding: 2rem 0;">
                            No subjects added yet. <a href="planner.html">Add subjects in the planner</a> to generate your timetable!
                        </div>
                    `;
                } else {
                    let html = '<div class="table-responsive"><table class="data-table"><thead><tr><th>Subject Name</th><th>Priority</th></tr></thead><tbody>';
                    subjects.forEach(sub => {
                        const badgeClass = sub.priority.toLowerCase() === 'high' ? 'badge-high' :
                                           (sub.priority.toLowerCase() === 'low' ? 'badge-low' : 'badge-medium');
                        html += `
                            <tr>
                                <td style="font-weight: 600;">${escapeHtml(sub.subjectName)}</td>
                                <td><span class="badge ${badgeClass}">${escapeHtml(sub.priority)}</span></td>
                            </tr>
                        `;
                    });
                    html += '</tbody></table></div>';
                    subjectContainer.innerHTML = html;
                }
            }
        }
    } catch (e) {
        console.error('Error fetching dashboard data:', e);
    }
}

// ==========================================
// PLANNER PAGE LOGIC (SUBJECTS & SCHEDULE GEN)
// ==========================================
let currentSubjects = [];

async function initPlannerPage() {
    const user = requireAuth();
    if (!user) return;

    await loadPlannerSubjects(user.userId);

    // Subject Add Form
    const subjectForm = document.getElementById('addSubjectForm');
    if (subjectForm) {
        subjectForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            clearAlert('subjectAlert');

            const nameInput = document.getElementById('subjectNameInput');
            const priorityInput = document.getElementById('subjectPriorityInput');

            const subjectName = nameInput.value.trim();
            const priority = priorityInput.value;

            if (!subjectName) {
                showAlert('subjectAlert', 'Please enter a subject name.');
                return;
            }

            try {
                const res = await fetch(`${API_BASE}/subjects/user/${user.userId}`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ subjectName, priority })
                });

                if (res.ok) {
                    nameInput.value = '';
                    priorityInput.value = 'Medium';
                    await loadPlannerSubjects(user.userId);
                    showAlert('subjectAlert', 'Subject added successfully!', 'success');
                    setTimeout(() => clearAlert('subjectAlert'), 3000);
                } else {
                    const err = await res.json();
                    showAlert('subjectAlert', err.message || 'Failed to add subject.');
                }
            } catch (err) {
                showAlert('subjectAlert', 'Network error while adding subject.');
            }
        });
    }

    // Generate Schedule Form
    const scheduleForm = document.getElementById('generateScheduleForm');
    if (scheduleForm) {
        scheduleForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            clearAlert('plannerAlert');

            const hoursInput = document.getElementById('studyHours');
            const timeInput = document.getElementById('startTime');

            const studyHours = parseInt(hoursInput.value, 10);
            const startTime = timeInput.value;

            if (!studyHours || studyHours <= 0) {
                showAlert('plannerAlert', 'Available study hours must be greater than zero.');
                return;
            }

            if (!startTime) {
                showAlert('plannerAlert', 'Please select a valid starting time.');
                return;
            }

            if (currentSubjects.length === 0) {
                showAlert('plannerAlert', 'Please add at least one subject below before generating the schedule.');
                return;
            }

            const genBtn = document.getElementById('generateScheduleBtn');
            genBtn.disabled = true;
            genBtn.textContent = 'Generating Timetable...';

            try {
                const res = await fetch(`${API_BASE}/schedules/generate`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        userId: user.userId,
                        studyHours: studyHours,
                        startTime: startTime,
                        breakMinutes: 15
                    })
                });

                const data = await res.json();
                if (res.ok && data.success) {
                    window.location.href = 'schedule.html';
                } else {
                    showAlert('plannerAlert', data.message || 'Failed to generate schedule.');
                }
            } catch (err) {
                showAlert('plannerAlert', 'Server connection error. Please make sure the backend is running.');
            } finally {
                genBtn.disabled = false;
                genBtn.textContent = '⚡ Generate Schedule';
            }
        });
    }
}

async function loadPlannerSubjects(userId) {
    try {
        const res = await fetch(`${API_BASE}/subjects/user/${userId}`);
        if (res.ok) {
            currentSubjects = await res.json();
            renderSubjectsTable(userId);
        }
    } catch (e) {
        console.error('Failed to load subjects', e);
    }
}

function renderSubjectsTable(userId) {
    const container = document.getElementById('subjectsTableContainer');
    if (!container) return;

    if (currentSubjects.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; color: var(--text-muted); padding: 2rem;">
                No subjects added yet. Use the form above to add your first subject!
            </div>
        `;
        return;
    }

    let html = `
        <div class="table-responsive">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Subject</th>
                        <th>Priority</th>
                        <th style="text-align: right;">Actions</th>
                    </tr>
                </thead>
                <tbody>
    `;

    currentSubjects.forEach(s => {
        const badgeClass = s.priority.toLowerCase() === 'high' ? 'badge-high' :
                           (s.priority.toLowerCase() === 'low' ? 'badge-low' : 'badge-medium');
        html += `
            <tr id="subject-row-${s.id}">
                <td>
                    <span id="subj-name-text-${s.id}" style="font-weight: 600;">${escapeHtml(s.subjectName)}</span>
                    <input type="text" id="subj-name-edit-${s.id}" class="form-control" style="display:none; width:auto;" value="${escapeHtml(s.subjectName)}" />
                </td>
                <td>
                    <span id="subj-priority-badge-${s.id}" class="badge ${badgeClass}">${escapeHtml(s.priority)}</span>
                    <select id="subj-priority-edit-${s.id}" class="form-control" style="display:none; width:auto;">
                        <option value="High" ${s.priority === 'High' ? 'selected' : ''}>High</option>
                        <option value="Medium" ${s.priority === 'Medium' ? 'selected' : ''}>Medium</option>
                        <option value="Low" ${s.priority === 'Low' ? 'selected' : ''}>Low</option>
                    </select>
                </td>
                <td style="text-align: right; white-space: nowrap;">
                    <button class="btn btn-sm btn-secondary" id="subj-edit-btn-${s.id}" onclick="toggleEditSubject(${s.id})">Edit</button>
                    <button class="btn btn-sm btn-success" id="subj-save-btn-${s.id}" style="display:none;" onclick="saveEditSubject(${s.id}, ${userId})">Save</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteSubject(${s.id}, ${userId})">Delete</button>
                </td>
            </tr>
        `;
    });

    html += `</tbody></table></div>`;
    container.innerHTML = html;
}

function toggleEditSubject(id) {
    const nameText = document.getElementById(`subj-name-text-${id}`);
    const nameEdit = document.getElementById(`subj-name-edit-${id}`);
    const badge = document.getElementById(`subj-priority-badge-${id}`);
    const prioEdit = document.getElementById(`subj-priority-edit-${id}`);
    const editBtn = document.getElementById(`subj-edit-btn-${id}`);
    const saveBtn = document.getElementById(`subj-save-btn-${id}`);

    const isEditing = nameEdit.style.display !== 'none';

    if (isEditing) {
        nameText.style.display = 'inline';
        nameEdit.style.display = 'none';
        badge.style.display = 'inline-flex';
        prioEdit.style.display = 'none';
        editBtn.textContent = 'Edit';
        saveBtn.style.display = 'none';
    } else {
        nameText.style.display = 'none';
        nameEdit.style.display = 'inline-block';
        badge.style.display = 'none';
        prioEdit.style.display = 'inline-block';
        editBtn.textContent = 'Cancel';
        saveBtn.style.display = 'inline-block';
    }
}

async function saveEditSubject(id, userId) {
    const nameEdit = document.getElementById(`subj-name-edit-${id}`).value.trim();
    const prioEdit = document.getElementById(`subj-priority-edit-${id}`).value;

    if (!nameEdit) {
        alert('Subject name cannot be empty.');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/subjects/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ subjectName: nameEdit, priority: prioEdit })
        });

        if (res.ok) {
            await loadPlannerSubjects(userId);
        } else {
            alert('Failed to update subject.');
        }
    } catch (e) {
        alert('Error updating subject.');
    }
}

async function deleteSubject(id, userId) {
    if (!confirm('Are you sure you want to delete this subject?')) return;

    try {
        const res = await fetch(`${API_BASE}/subjects/${id}`, { method: 'DELETE' });
        if (res.ok) {
            await loadPlannerSubjects(userId);
        } else {
            alert('Failed to delete subject.');
        }
    } catch (e) {
        alert('Error deleting subject.');
    }
}

// ==========================================
// SCHEDULE / TIME-BAR PAGE LOGIC
// ==========================================
async function initSchedulePage() {
    const user = requireAuth();
    if (!user) return;

    await loadScheduleAndProgress(user.userId);
}

async function loadScheduleAndProgress(userId) {
    try {
        const [schedRes, progRes] = await Promise.all([
            fetch(`${API_BASE}/schedules/user/${userId}`),
            fetch(`${API_BASE}/schedules/progress/${userId}`)
        ]);

        if (progRes.ok) {
            const prog = await progRes.json();
            const pct = Math.round(prog.completionPercentage || 0);
            const pBar = document.getElementById('schedProgressBarFill');
            const pText = document.getElementById('schedProgressPercent');
            if (pBar) pBar.style.width = `${pct}%`;
            if (pText) pText.textContent = `${pct}%`;

            const sTotal = document.getElementById('schedTotalSessions');
            const sCompleted = document.getElementById('schedCompletedSessions');
            const sPending = document.getElementById('schedPendingSessions');
            if (sTotal) sTotal.textContent = prog.totalSessions;
            if (sCompleted) sCompleted.textContent = prog.completedSessions;
            if (sPending) sPending.textContent = prog.pendingSessions;
        }

        if (schedRes.ok) {
            const schedule = await schedRes.json();
            renderTimeBarSummary(schedule);
            renderTimeline(schedule, userId);
        }
    } catch (err) {
        console.error('Error fetching schedule:', err);
    }
}

function renderTimeBarSummary(schedule) {
    const container = document.getElementById('timebarSummary');
    if (!container) return;

    if (!schedule || schedule.length === 0) {
        container.style.display = 'none';
        return;
    }
    container.style.display = 'flex';
    container.innerHTML = '';

    schedule.forEach(item => {
        const seg = document.createElement('div');
        seg.className = 'timebar-segment';

        if (item.break) {
            seg.classList.add('break-seg');
            seg.style.flex = '1'; // 15 mins
            seg.title = `${item.startTime} - ${item.endTime}: Break`;
            seg.textContent = '☕ Break';
        } else {
            seg.style.flex = '4'; // 60 mins
            const p = (item.priority || 'Medium').toLowerCase();
            seg.classList.add(`study-${p}`);
            seg.title = `${item.startTime} - ${item.endTime}: ${item.subjectName} (${item.priority})`;
            seg.textContent = item.subjectName;
        }

        container.appendChild(seg);
    });
}

function renderTimeline(schedule, userId) {
    const container = document.getElementById('timelineContainer');
    if (!container) return;

    if (!schedule || schedule.length === 0) {
        container.innerHTML = `
            <div class="card" style="text-align: center; padding: 3rem 1.5rem;">
                <h3 style="margin-bottom: 0.5rem;">No Schedule Generated Yet</h3>
                <p style="color: var(--text-muted); margin-bottom: 1.5rem;">Set your study hours and subjects in the planner to generate your automatic timetable.</p>
                <a href="planner.html" class="btn btn-primary">Go to Study Planner</a>
            </div>
        `;
        return;
    }

    let html = '';
    schedule.forEach(item => {
        if (item.break) {
            html += `
                <div class="break-card">
                    <div class="break-content">
                        <span class="break-icon">☕</span>
                        <div>
                            <span>15-Minute Refresh Break</span>
                            <div style="font-size: 0.75rem; color: #047857; font-weight: normal;">Rest your eyes, hydrate, and stretch.</div>
                        </div>
                    </div>
                    <div class="break-time">${escapeHtml(item.startTime)} - ${escapeHtml(item.endTime)}</div>
                </div>
            `;
        } else {
            const isDone = item.status === 'Completed';
            const badgePrio = (item.priority || 'Medium').toLowerCase();
            const badgePrioClass = badgePrio === 'high' ? 'badge-high' :
                                   (badgePrio === 'low' ? 'badge-low' : 'badge-medium');

            html += `
                <div class="session-card ${isDone ? 'completed' : ''}" id="sched-card-${item.id}">
                    <div class="session-time">
                        <div class="time-range">${escapeHtml(item.startTime)} - ${escapeHtml(item.endTime)}</div>
                        <div class="time-duration">⏱ 1 Hour Session</div>
                    </div>
                    <div class="session-main">
                        <div class="session-name">${escapeHtml(item.subjectName)}</div>
                        <div class="session-badges">
                            <span class="badge ${badgePrioClass}">${escapeHtml(item.priority || 'Medium')} Priority</span>
                            <span class="badge ${isDone ? 'badge-completed' : 'badge-pending'}">${isDone ? '✓ Completed' : 'Pending'}</span>
                        </div>
                    </div>
                    <div>
                        <button class="btn btn-sm ${isDone ? 'btn-success' : 'btn-primary'}" onclick="toggleCompleteSession(${item.id}, ${userId})">
                            ${isDone ? '✓ Completed' : 'Mark Complete'}
                        </button>
                    </div>
                </div>
            `;
        }
    });

    container.innerHTML = html;
}

async function toggleCompleteSession(scheduleId, userId) {
    try {
        const res = await fetch(`${API_BASE}/schedules/${scheduleId}/complete`, {
            method: 'PUT'
        });

        if (res.ok) {
            await loadScheduleAndProgress(userId);
        } else {
            alert('Failed to update session completion status.');
        }
    } catch (e) {
        alert('Network error while completing session.');
    }
}

async function clearUserSchedule() {
    const user = requireAuth();
    if (!user) return;

    if (!confirm('Are you sure you want to clear your current schedule? You can regenerate a new one anytime.')) {
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/schedules/user/${user.userId}`, {
            method: 'DELETE'
        });

        if (res.ok) {
            await loadScheduleAndProgress(user.userId);
        }
    } catch (e) {
        alert('Error clearing schedule.');
    }
}
