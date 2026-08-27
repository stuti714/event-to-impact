const state = {
    year: 2026,
    events: [],
    risk: new Map(),
    recommendations: [],
    lastPreferences: null,
    modelCard: null
};

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];

document.addEventListener('DOMContentLoaded', async () => {
    bindNavigation();
    bindPlanner();
    bindFilters();
    bindDialog();
    startClock();
    await Promise.all([loadInsights(), loadAlerts(), loadEvents(), loadModelCard()]);
});

async function api(url, options = {}) {
    const request = { ...options };
    const headers = { ...(options.headers || {}) };
    const method = (options.method || 'GET').toUpperCase();
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    if (!['GET', 'HEAD', 'OPTIONS'].includes(method) && csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }
    request.headers = headers;
    request.credentials = 'same-origin';
    const response = await fetch(url, request);
    if (!response.ok) throw new Error(`Request failed (${response.status})`);
    return response.json();
}

function bindNavigation() {
    $$('.nav-link').forEach(button => button.addEventListener('click', () => {
        document.getElementById(button.dataset.target)?.scrollIntoView({ behavior: 'smooth' });
        $$('.nav-link').forEach(item => item.classList.remove('active'));
        button.classList.add('active');
        $('#sidebar').classList.remove('open');
    }));
    $('#menuButton').addEventListener('click', () => $('#sidebar').classList.toggle('open'));
    $$('.jump-planner').forEach(button => button.addEventListener('click', () => $('#planner').scrollIntoView({ behavior: 'smooth' })));
    $('#browseEvents')?.addEventListener('click', () => $('#calendar').scrollIntoView({ behavior: 'smooth' }));
    $('#openAlerts')?.addEventListener('click', () => $('#alerts').scrollIntoView({ behavior: 'smooth' }));
    $('#openMethod').addEventListener('click', () => $('#method').scrollIntoView({ behavior: 'smooth' }));
    $('#methodDetails').addEventListener('click', () => showToast('Open docs/METHODOLOGY.md in the project for the complete formula and limitations.'));
}

function bindPlanner() {
    $$('.choice-chip').forEach(chip => chip.addEventListener('click', () => chip.classList.toggle('selected')));
    $('#plannerForm').addEventListener('submit', async event => {
        event.preventDefault();
        const button = $('.recommend-button');
        const original = button.innerHTML;
        button.disabled = true;
        button.textContent = 'Finding meaningful events…';
        const year = Number($('#planYear').value);
        const todayInIndia = new Intl.DateTimeFormat('en-CA', {
            year: 'numeric', month: '2-digit', day: '2-digit', timeZone: 'Asia/Kolkata'
        }).format(new Date());
        const fromDate = year === Number(todayInIndia.slice(0, 4)) ? todayInIndia : `${year}-01-01`;
        const payload = {
            interests: $$('.choice-chip.selected').map(chip => chip.dataset.value),
            maxBudget: 0,
            crowdTolerance: $('#crowdTolerance').value,
            companions: $('#companions').value,
            accessibleOnly: $('#accessibleOnly').checked,
            environment: $('#environment').value,
            from: fromDate,
            to: `${year}-12-31`
        };
        state.lastPreferences = payload;
        try {
            state.recommendations = await api('/api/recommendations', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            renderRecommendations();
            await loadModelCard();
            $('#recommendationPanel').classList.remove('hidden');
            $('#recommendationPanel').scrollIntoView({ behavior: 'smooth', block: 'start' });
        } catch (error) {
            showToast('Could not create your Action Brief. Please refresh and try again.');
        } finally {
            button.disabled = false;
            button.innerHTML = original;
        }
    });
    $('#clearRecommendations').addEventListener('click', () => {
        $('#recommendationPanel').classList.add('hidden');
        $('#plannerForm').scrollIntoView({ behavior: 'smooth', block: 'center' });
    });
}

function bindFilters() {
    $$('.year-tab').forEach(tab => tab.addEventListener('click', async () => {
        state.year = Number(tab.dataset.year);
        $$('.year-tab').forEach(item => item.classList.toggle('active', item === tab));
        await loadEvents();
    }));
    let timer;
    $('#searchInput').addEventListener('input', () => {
        clearTimeout(timer);
        timer = setTimeout(loadEvents, 240);
    });
    $('#categoryFilter').addEventListener('change', loadEvents);
    $('#freeFilter').addEventListener('change', loadEvents);
}

function bindDialog() {
    $('.dialog-close').addEventListener('click', () => $('#eventDialog').close());
    $('#eventDialog').addEventListener('click', event => {
        if (event.target === $('#eventDialog')) $('#eventDialog').close();
    });
}

async function loadInsights() {
    try {
        const insights = await api('/api/insights');
        setTextIfPresent('totalEvents', insights.totalEvents);
        setTextIfPresent('verifiedDates', insights.verifiedDates);
        setTextIfPresent('freeEvents', insights.freeEvents);
        setTextIfPresent('tentativeDates', insights.tentativeDates);
    } catch (error) {
        ['totalEvents', 'verifiedDates', 'freeEvents', 'tentativeDates'].forEach(id => setTextIfPresent(id, '—'));
    }
}

async function loadEvents() {
    const params = new URLSearchParams({ year: state.year });
    const query = $('#searchInput')?.value.trim();
    const category = $('#categoryFilter')?.value;
    if (query) params.set('q', query);
    if (category) params.set('category', category);
    if ($('#freeFilter')?.checked) params.set('freeOnly', 'true');
    $('#eventTimeline').innerHTML = '<div class="skeleton"></div><div class="skeleton"></div>';
    try {
        state.events = await api(`/api/events?${params}`);
        await Promise.all(state.events.map(async event => {
            if (!state.risk.has(event.id)) state.risk.set(event.id, await api(`/api/events/${event.id}/risk`));
        }));
        renderTimeline();
    } catch (error) {
        $('#eventTimeline').innerHTML = '<div class="empty-state"><strong>Could not load the calendar.</strong><br>Confirm that Event to Impact is running on port 8082.</div>';
    }
}

function renderTimeline() {
    $('#resultCount').textContent = `${state.events.length} record${state.events.length === 1 ? '' : 's'}`;
    if (!state.events.length) {
        $('#eventTimeline').innerHTML = '<div class="empty-state">No events match these filters. Try another impact area or year.</div>';
        return;
    }
    const groups = state.events.reduce((result, event) => {
        const date = new Date(event.startTime);
        const month = date.toLocaleDateString('en-IN', { month: 'long' });
        (result[month] ||= []).push(event);
        return result;
    }, {});
    $('#eventTimeline').innerHTML = Object.entries(groups).map(([month, events]) => `
        <section class="month-group">
            <div class="month-label"><strong>${month}</strong><span>${events.length} planning record${events.length > 1 ? 's' : ''}</span></div>
            <div class="event-grid">${events.map(event => eventCard(event, state.risk.get(event.id))).join('')}</div>
        </section>`).join('');
    bindCardButtons($('#eventTimeline'));
}

function renderRecommendations() {
    const grid = $('#recommendationGrid');
    if (!state.recommendations.length) {
        grid.innerHTML = '<div class="empty-state">No suitable event was found. Broaden your interests or optional filters.</div>';
        return;
    }
    const best = state.recommendations[0];
    const backup = state.recommendations[1];
    const alternatives = state.recommendations.slice(1, 4);
    const event = best.event;
    const risk = best.risk;
    const mode = best.modelMode || 'RULE_FALLBACK';
    const modeLabel = modelModeLabel(mode);
    const contentValue = mode === 'RULE_FALLBACK' ? 'Not active' : `${best.contentScore}%`;
    const learnedValue = best.learnedScore == null ? 'Collecting feedback' : `${best.learnedScore}%`;
    grid.innerHTML = `
        <article class="visit-brief" id="visitBrief">
            <div class="brief-top">
                <div><span class="brief-kicker">YOUR BEST-FIT EVENT</span><span class="result-model-badge ${mode.toLowerCase()}">${escapeHtml(modeLabel)}</span><h2>${escapeHtml(event.title)}</h2><p>${formatEventDate(event)} · ${escapeHtml(event.participationMode || 'Check organiser')}</p></div>
                <div class="match-orb"><div><strong>${best.matchScore}%</strong><small>RANKING SCORE</small></div></div>
            </div>
            <div class="brief-body">
                <div class="brief-facts">
                    <div><small>PARTICIPATION</small><strong>${escapeHtml(event.participationMode || 'Check organiser')}</strong></div>
                    <div><small>PUBLIC ACTIVITY</small><strong>${risk.level} · ${risk.score}/100</strong></div>
                    <div><small>NEXT STEP</small><strong>${escapeHtml(risk.bestArrivalTime)}</strong></div>
                    <div><small>PROGRAMME CONFIDENCE</small><strong>${escapeHtml(risk.confidence)}</strong></div>
                    <div><small>DATE STATUS</small><strong>${dateStatusLabel(event.dateStatus)}</strong></div>
                </div>
                <div class="impact-strip">
                    <div><small>WHY IT MATTERS</small><strong>${escapeHtml(event.impactGoal || event.description)}</strong></div>
                    <div><small>WHO IT HELPS</small><strong>${escapeHtml(event.audience || 'People interested in this topic')}</strong></div>
                </div>
                <div class="brief-explanation">
                    <div class="why-fit"><h3>Why this fits you</h3><ul>${best.matchReasons.slice(0, 4).map(reason => `<li>${escapeHtml(reason)}</li>`).join('')}${risk.reasons.slice(0, 2).map(reason => `<li>${escapeHtml(reason)}</li>`).join('')}</ul></div>
                    <div class="brief-trust"><h3>Source confidence</h3><p>${escapeHtml(risk.attendanceNote)} The schedule was reviewed on 02 Aug 2026.</p><a href="${escapeAttribute(event.sourceUrl)}" target="_blank" rel="noopener noreferrer">Check ${escapeHtml(event.sourceName)} ↗</a></div>
                </div>
                <div class="model-proof">
                    <div><small>CONTENT RELEVANCE</small><strong>${contentValue}</strong><span>TF-IDF + cosine similarity</span></div>
                    <div><small>PRACTICAL FIT</small><strong>${best.constraintScore ?? best.matchScore}%</strong><span>Year, companions and participation preferences</span></div>
                    <div><small>LEARNED PREFERENCE</small><strong>${learnedValue}</strong><span>${best.learnedScore == null ? 'Activates after 10 mixed feedback samples' : 'Logistic regression probability'}</span></div>
                </div>
                <div class="feedback-row" aria-label="Recommendation feedback">
                    <div><strong>Help this model learn</strong><span>Your clicks become labeled preference data—never attendance data.</span></div>
                    <div class="feedback-buttons"><button data-feedback="INTERESTED" data-event-id="${event.id}">Interested</button><button data-feedback="SAVED" data-event-id="${event.id}">Save event</button><button data-feedback="NOT_FOR_ME" data-event-id="${event.id}">Not for me</button></div>
                </div>
                <div class="brief-actions">
                    <span class="backup-plan">${backup ? `Another useful match: <strong>${escapeHtml(backup.event.title)}</strong>` : 'Broaden your preferences to reveal another useful event.'}</span>
                    <div class="brief-buttons"><button class="button secondary view-event" data-event-id="${event.id}">Full reasoning</button><button class="button primary" id="printVisitBrief">Print / Save PDF</button></div>
                </div>
            </div>
        </article>
        ${alternatives.length ? `<section class="alternatives-block"><h3 class="alternatives-heading">Other events worth considering</h3><div class="alternatives-grid">${alternatives.map(result => eventCard(result.event, result.risk, result)).join('')}</div></section>` : ''}`;
    bindCardButtons(grid);
    bindFeedbackButtons(grid);
    $('#printVisitBrief')?.addEventListener('click', () => window.print());
}

function bindFeedbackButtons(root) {
    $$('[data-feedback]', root).forEach(button => button.addEventListener('click', async () => {
        const group = $('.feedback-buttons', root);
        $$('button', group).forEach(item => item.disabled = true);
        try {
            const response = await api('/api/feedback', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    eventId: Number(button.dataset.eventId),
                    action: button.dataset.feedback,
                    preferences: state.lastPreferences
                })
            });
            button.classList.add('chosen');
            showToast(response.message);
            await loadModelCard();
        } catch (error) {
            $$('button', group).forEach(item => item.disabled = false);
            showToast('Feedback could not be saved. Please try again.');
        }
    }));
}

async function loadModelCard() {
    try {
        const card = await api('/api/model-card');
        state.modelCard = card;
        setTextIfPresent('modelMode', modelModeLabel(card.modelMode));
        setTextIfPresent('modelVersion', card.modelVersion || '—');
        setTextIfPresent('modelAlgorithm', card.algorithm);
        setTextIfPresent('indexedEvents', card.indexedEvents);
        setTextIfPresent('vocabularySize', card.vocabularySize);
        setTextIfPresent('feedbackSamples', `${card.feedbackSamples}/${card.minimumFeedback}`);
        $('#modelStatusCard')?.classList.toggle('offline', card.status !== 'READY');
        $('#modelStatusDot')?.classList.toggle('online', card.status === 'READY');
    } catch (error) {
        setTextIfPresent('modelMode', 'Rule fallback');
        setTextIfPresent('modelAlgorithm', 'Model status is temporarily unavailable.');
        $('#modelStatusCard')?.classList.add('offline');
    }
}

function modelModeLabel(mode) {
    return ({
        CONTENT_BASED: 'Content ML active',
        HYBRID_LEARNED: 'Hybrid learning active',
        RULE_FALLBACK: 'Rule fallback'
    })[mode] || String(mode || 'Unknown mode').replaceAll('_', ' ');
}

function eventCard(event, risk, recommendation) {
    const tentative = isTentative(event);
    const tags = (event.tags || '').split(',').slice(0, 3);
    return `
        <article class="event-card ${event.featured ? 'featured' : ''}">
            ${recommendation ? `<div class="match-banner"><span>WHY IT FITS</span><b>${recommendation.matchScore}% match</b></div>` : ''}
            <div class="card-top">
                <span class="category-pill">${escapeHtml(event.category)}</span>
                <span class="date-status ${tentative ? 'tentative' : 'verified'}">${dateStatusLabel(event.dateStatus)}</span>
            </div>
            <h3>${escapeHtml(event.title)}</h3>
            <div class="event-date">${formatEventDate(event)}</div>
            <p class="event-place">${escapeHtml(event.participationMode || event.venue)} · ${escapeHtml(event.area)}</p>
            ${recommendation ? `<div class="card-tags">${recommendation.matchReasons.slice(0, 2).map(reason => `<span>${escapeHtml(reason)}</span>`).join('')}</div>` : `<div class="card-tags">${tags.map(tag => `<span>${escapeHtml(tag)}</span>`).join('')}</div>`}
            <div class="card-bottom">
                <span class="price">${event.freeEntry ? 'Free participation' : `₹${Number(event.price).toLocaleString('en-IN')}`}</span>
                <span class="risk-pill ${risk.level.toLowerCase()}">${risk.level} activity</span>
                <button class="view-event" data-event-id="${event.id}">Why it matters →</button>
            </div>
        </article>`;
}

function bindCardButtons(root) {
    $$('.view-event', root).forEach(button => button.addEventListener('click', () => openEvent(Number(button.dataset.eventId))));
}

async function openEvent(id) {
    try {
        let event = state.events.find(item => item.id === id)
            || state.recommendations.find(item => item.event.id === id)?.event;
        if (!event) event = await api(`/api/events/${id}`);
        let risk = state.risk.get(id) || state.recommendations.find(item => item.event.id === id)?.risk;
        if (!risk) risk = await api(`/api/events/${id}/risk`);
        state.risk.set(id, risk);
        $('#dialogContent').innerHTML = dialogMarkup(event, risk);
        $('#eventDialog').showModal();
    } catch (error) {
        showToast('Could not open that event. Please try again.');
    }
}

function dialogMarkup(event, risk) {
    return `
        <div class="dialog-hero">
            <span class="category-pill">${escapeHtml(event.category)}</span>
            <h2>${escapeHtml(event.title)}</h2>
            <p>${formatEventDate(event)} · ${escapeHtml(event.participationMode || 'Check organiser')}</p>
        </div>
        <div class="dialog-body">
            <div class="dialog-meta">
                <div><small>PARTICIPATION</small><strong>${escapeHtml(event.participationMode || 'Check organiser')}</strong></div>
                <div><small>NEXT STEP</small><strong>${escapeHtml(risk.bestArrivalTime)}</strong></div>
                <div><small>DATE STATUS</small><strong>${dateStatusLabel(event.dateStatus)}</strong></div>
            </div>
            <p class="dialog-description">${escapeHtml(event.description)}</p>
            <div class="impact-strip dialog-impact">
                <div><small>WHY IT MATTERS</small><strong>${escapeHtml(event.impactGoal || event.description)}</strong></div>
                <div><small>WHO IT HELPS</small><strong>${escapeHtml(event.audience || 'People interested in this topic')}</strong></div>
            </div>
            <div class="risk-box">
                <div class="risk-box-head"><div><span class="eyebrow">COMPARATIVE PUBLIC-ACTIVITY CONTEXT</span><h3>${risk.level} · ${risk.score}/100</h3></div><span class="risk-pill ${risk.level.toLowerCase()}">${risk.confidence} confidence</span></div>
                <div class="score-track"><i style="width:${risk.score}%"></i></div>
                <strong>Why this band?</strong>
                <ul>${risk.reasons.map(reason => `<li>${escapeHtml(reason)}</li>`).join('')}</ul>
                <div class="attendance-note">ⓘ ${escapeHtml(risk.attendanceNote)}</div>
            </div>
            <div class="source-box"><strong>Source transparency</strong><br>${escapeHtml(event.sourceName)} · reviewed 02 Aug 2026 · ${escapeHtml(event.verificationStatus.replaceAll('_', ' '))}<br><a href="${escapeAttribute(event.sourceUrl)}" target="_blank" rel="noopener noreferrer">Open official/source page ↗</a></div>
            <div class="dialog-actions"><button class="button primary" onclick="document.getElementById('eventDialog').close()">Done</button></div>
        </div>`;
}

async function loadAlerts() {
    try {
        const alerts = await api('/api/alerts');
        const visible = alerts.slice(0, 7);
        ['navAlertCount', 'panelAlertCount'].forEach(id => document.getElementById(id).textContent = alerts.length);
        $('#alertList').innerHTML = visible.map(alert => `
            <div class="alert-item ${alert.severity.toLowerCase()}">
                <span class="alert-symbol">${alert.severity === 'WARNING' ? '!' : alert.severity === 'VERIFY' ? '?' : 'i'}</span>
                <div><strong>${escapeHtml(alert.title)}</strong><small>${escapeHtml(alert.message)}</small></div>
                <button data-alert-event="${alert.eventId}">${escapeHtml(alert.action)} →</button>
            </div>`).join('') || '<div class="empty-state">No planning alerts right now.</div>';
        $$('[data-alert-event]').forEach(button => button.addEventListener('click', () => openEvent(Number(button.dataset.alertEvent))));
    } catch (error) {
        $('#alertList').innerHTML = '<div class="empty-state">Alerts are temporarily unavailable.</div>';
    }
}

function isTentative(event) {
    return ['TENTATIVE_WINDOW', 'DATES_TBA'].includes(event.dateStatus);
}

function dateStatusLabel(status) {
    return ({
        VERIFIED: 'Verified date',
        DATE_VERIFIED: 'Verified date',
        FIXED_ANNUAL_DATE: 'Fixed annual date',
        CURATED_VISIT_DATE: 'Suggested visit slot',
        TENTATIVE_WINDOW: 'Tentative window',
        DATES_TBA: 'Dates TBA'
    })[status] || 'Check source';
}

function formatEventDate(event) {
    const start = new Date(event.startTime);
    const end = new Date(event.endTime);
    if (isTentative(event)) {
        const first = start.toLocaleDateString('en-IN', { month: 'short' });
        const lastMonth = end.toLocaleDateString('en-IN', { month: 'short' });
        const year = end.getFullYear();
        return `Expected ${first}${first === lastMonth ? ` ${year}` : `–${lastMonth} ${year}`} · dates TBA`;
    }
    const startText = start.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
    const sameDay = start.toDateString() === end.toDateString();
    if (sameDay) return `${startText} · ${start.toLocaleTimeString('en-IN', { hour: 'numeric', minute: '2-digit' })}`;
    return `${startText} – ${end.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}`;
}

function startClock() {
    const update = () => {
        $('#currentTime').textContent = new Intl.DateTimeFormat('en-IN', {
            hour: '2-digit', minute: '2-digit', hour12: true, timeZone: 'Asia/Kolkata'
        }).format(new Date());
    };
    update();
    setInterval(update, 30000);
}

function showToast(message) {
    const toast = $('#toast');
    toast.textContent = message;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 3200);
}

function setTextIfPresent(id, value) {
    const element = document.getElementById(id);
    if (element) element.textContent = value;
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>'"]/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[character]);
}

function escapeAttribute(value) {
    try {
        const url = new URL(String(value));
        return ['http:', 'https:'].includes(url.protocol) ? escapeHtml(url.href) : '#';
    } catch { return '#'; }
}
