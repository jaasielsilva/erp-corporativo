/**
 * JavaScript Específico para Módulo de Agenda - ERP Corporativo
 * Funcionalidades: Calendário, Eventos, Lembretes, Validações e Interações
 */

// Configurações globais
const AgendaConfig = {
    baseUrl: '/agenda',
    dateFormat: 'DD/MM/YYYY',
    timeFormat: 'HH:mm',
    dateTimeFormat: 'DD/MM/YYYY HH:mm',
    locale: 'pt-BR',
    timezone: 'America/Sao_Paulo',
    autoSave: true,
    autoSaveInterval: 30000, // 30 segundos
    maxFileSize: 5 * 1024 * 1024, // 5MB
    allowedFileTypes: ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'jpg', 'jpeg', 'png', 'gif']
};

// Classe principal da Agenda
class AgendaManager {
    constructor() {
        this.calendar = null;
        this.eventos = [];
        this.lembretes = [];
        this.filtros = {};
        this.autoSaveTimer = null;
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.initializeCalendar();
        this.loadEventos();
        this.loadLembretes();
        this.setupAutoSave();
        this.setupNotifications();
        this.setupValidations();
    }

    setupEventListeners() {
        // Eventos de formulário
        $(document).on('submit', '.agenda-form', this.handleFormSubmit.bind(this));
        $(document).on('click', '.btn-save-draft', this.saveDraft.bind(this));
        $(document).on('click', '.btn-delete-evento', this.deleteEvento.bind(this));
        $(document).on('click', '.btn-duplicate-evento', this.duplicateEvento.bind(this));
        
        // Eventos de filtro
        $(document).on('change', '.filtro-agenda', this.applyFilters.bind(this));
        $(document).on('click', '.btn-clear-filters', this.clearFilters.bind(this));
        
        // Eventos de modal
        $(document).on('show.bs.modal', '.agenda-modal', this.onModalShow.bind(this));
        $(document).on('hidden.bs.modal', '.agenda-modal', this.onModalHide.bind(this));
        
        // Eventos de drag and drop
        $(document).on('dragstart', '.evento-item', this.onDragStart.bind(this));
        $(document).on('dragover', '.calendar-drop-zone', this.onDragOver.bind(this));
        $(document).on('drop', '.calendar-drop-zone', this.onDrop.bind(this));
        
        // Eventos de teclado
        $(document).on('keydown', this.handleKeyboardShortcuts.bind(this));
        
        // Eventos de notificação
        $(document).on('click', '.notification-item', this.handleNotificationClick.bind(this));
        
        // Eventos de exportação
        $(document).on('click', '.btn-export', this.handleExport.bind(this));
        
        // Eventos de busca
        $(document).on('input', '.search-eventos', this.debounce(this.searchEventos.bind(this), 300));
    }

    initializeCalendar() {
        if (typeof FullCalendar === 'undefined') {
            console.warn('FullCalendar não está carregado');
            return;
        }

        const calendarEl = document.getElementById('calendar');
        if (!calendarEl) return;

        this.calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            locale: AgendaConfig.locale,
            timeZone: AgendaConfig.timezone,
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek'
            },
            buttonText: {
                today: 'Hoje',
                month: 'Mês',
                week: 'Semana',
                day: 'Dia',
                list: 'Lista'
            },
            height: 'auto',
            editable: true,
            selectable: true,
            selectMirror: true,
            dayMaxEvents: true,
            weekends: true,
            events: this.loadCalendarEvents.bind(this),
            select: this.handleDateSelect.bind(this),
            eventClick: this.handleEventClick.bind(this),
            eventDrop: this.handleEventDrop.bind(this),
            eventResize: this.handleEventResize.bind(this),
            eventDidMount: this.handleEventMount.bind(this),
            loading: this.handleCalendarLoading.bind(this)
        });

        this.calendar.render();
    }

    loadCalendarEvents(info, successCallback, failureCallback) {
        $.ajax({
            url: `${AgendaConfig.baseUrl}/eventos/calendar`,
            type: 'GET',
            data: {
                start: info.startStr,
                end: info.endStr,
                ...this.filtros
            },
            success: (data) => {
                const events = data.map(evento => ({
                    id: evento.id,
                    title: evento.titulo,
                    start: evento.dataInicio,
                    end: evento.dataFim,
                    allDay: evento.diaInteiro,
                    backgroundColor: this.getEventColor(evento.tipo, evento.prioridade),
                    borderColor: this.getEventColor(evento.tipo, evento.prioridade),
                    textColor: '#ffffff',
                    extendedProps: {
                        tipo: evento.tipo,
                        status: evento.status,
                        prioridade: evento.prioridade,
                        descricao: evento.descricao,
                        local: evento.local,
                        participantes: evento.participantes
                    }
                }));
                successCallback(events);
            },
            error: (xhr) => {
                console.error('Erro ao carregar eventos:', xhr.responseText);
                failureCallback(xhr);
            }
        });
    }

    getEventColor(tipo, prioridade) {
        const colors = {
            reuniao: '#4e73df',
            tarefa: '#1cc88a',
            compromisso: '#f6c23e',
            evento: '#36b9cc',
            pessoal: '#e74a3b'
        };

        const priorityColors = {
            alta: '#e74a3b',
            media: '#f6c23e',
            baixa: '#1cc88a'
        };

        return prioridade === 'alta' ? priorityColors[prioridade] : (colors[tipo] || '#858796');
    }

    handleDateSelect(selectInfo) {
        const modal = new bootstrap.Modal(document.getElementById('modalNovoEvento'));
        
        // Preencher datas selecionadas
        $('#dataInicio').val(moment(selectInfo.start).format('YYYY-MM-DDTHH:mm'));
        if (selectInfo.end) {
            $('#dataFim').val(moment(selectInfo.end).subtract(1, 'day').format('YYYY-MM-DDTHH:mm'));
        }
        
        // Marcar como dia inteiro se seleção for de dia completo
        if (selectInfo.allDay) {
            $('#diaInteiro').prop('checked', true);
            this.toggleTimeFields(false);
        }
        
        modal.show();
        this.calendar.unselect();
    }

    handleEventClick(clickInfo) {
        const evento = clickInfo.event;
        this.showEventDetails(evento.id, evento.extendedProps);
    }

    handleEventDrop(dropInfo) {
        this.updateEventDateTime(dropInfo.event.id, {
            dataInicio: dropInfo.event.start,
            dataFim: dropInfo.event.end
        });
    }

    handleEventResize(resizeInfo) {
        this.updateEventDateTime(resizeInfo.event.id, {
            dataInicio: resizeInfo.event.start,
            dataFim: resizeInfo.event.end
        });
    }

    handleEventMount(mountInfo) {
        // Adicionar tooltip aos eventos
        $(mountInfo.el).tooltip({
            title: this.generateEventTooltip(mountInfo.event),
            html: true,
            placement: 'top',
            container: 'body'
        });
    }

    handleCalendarLoading(isLoading) {
        if (isLoading) {
            this.showLoading();
        } else {
            this.hideLoading();
        }
    }

    generateEventTooltip(event) {
        const props = event.extendedProps;
        return `
            <div class="event-tooltip">
                <strong>${event.title}</strong><br>
                <small>
                    <i class="fas fa-clock"></i> ${moment(event.start).format(AgendaConfig.dateTimeFormat)}<br>
                    <i class="fas fa-tag"></i> ${props.tipo}<br>
                    <i class="fas fa-flag"></i> ${props.prioridade}<br>
                    ${props.local ? `<i class="fas fa-map-marker-alt"></i> ${props.local}<br>` : ''}
                    ${props.participantes ? `<i class="fas fa-users"></i> ${props.participantes.length} participante(s)` : ''}
                </small>
            </div>
        `;
    }

    loadEventos() {
        $.ajax({
            url: `${AgendaConfig.baseUrl}/eventos`,
            type: 'GET',
            data: this.filtros,
            success: (data) => {
                this.eventos = data;
                this.renderEventosList();
                this.updateStatistics();
            },
            error: (xhr) => {
                console.error('Erro ao carregar eventos:', xhr.responseText);
                this.showError('Erro ao carregar eventos');
            }
        });
    }

    loadLembretes() {
        $.ajax({
            url: `${AgendaConfig.baseUrl}/lembretes`,
            type: 'GET',
            success: (data) => {
                this.lembretes = data;
                this.renderLembretesList();
                this.checkPendingReminders();
            },
            error: (xhr) => {
                console.error('Erro ao carregar lembretes:', xhr.responseText);
            }
        });
    }

    renderEventosList() {
        const container = $('#eventosContainer');
        if (!container.length) return;

        let html = '';
        this.eventos.forEach(evento => {
            html += this.generateEventoCard(evento);
        });

        container.html(html);
        this.setupEventosInteractions();
    }

    generateEventoCard(evento) {
        const statusClass = this.getStatusClass(evento.status);
        const prioridadeClass = this.getPrioridadeClass(evento.prioridade);
        
        return `
            <div class="evento-item fade-in" data-evento-id="${evento.id}">
                <div class="d-flex justify-content-between align-items-start">
                    <div class="flex-grow-1">
                        <h6 class="evento-titulo">${evento.titulo}</h6>
                        <div class="evento-detalhes">
                            <div class="detail-item">
                                <i class="fas fa-calendar"></i>
                                <span>${moment(evento.dataInicio).format(AgendaConfig.dateTimeFormat)}</span>
                            </div>
                            <div class="detail-item">
                                <i class="fas fa-tag"></i>
                                <span>${evento.tipo}</span>
                            </div>
                            ${evento.local ? `
                                <div class="detail-item">
                                    <i class="fas fa-map-marker-alt"></i>
                                    <span>${evento.local}</span>
                                </div>
                            ` : ''}
                            ${evento.participantes && evento.participantes.length > 0 ? `
                                <div class="detail-item">
                                    <i class="fas fa-users"></i>
                                    <span>${evento.participantes.length} participante(s)</span>
                                </div>
                            ` : ''}
                        </div>
                    </div>
                    <div class="d-flex flex-column align-items-end gap-2">
                        <div class="d-flex gap-1">
                            <span class="agenda-badge ${statusClass}">${evento.status}</span>
                            <span class="agenda-badge ${prioridadeClass}">${evento.prioridade}</span>
                        </div>
                        <div class="btn-group btn-group-sm">
                            <button type="button" class="btn btn-outline-primary btn-sm" onclick="agendaManager.editEvento(${evento.id})">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button type="button" class="btn btn-outline-info btn-sm" onclick="agendaManager.duplicateEvento(${evento.id})">
                                <i class="fas fa-copy"></i>
                            </button>
                            <button type="button" class="btn btn-outline-danger btn-sm" onclick="agendaManager.deleteEvento(${evento.id})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    getStatusClass(status) {
        const classes = {
            agendado: 'status-agendado',
            concluido: 'status-concluido',
            cancelado: 'status-cancelado',
            adiado: 'status-adiado'
        };
        return classes[status] || 'status-agendado';
    }

    getPrioridadeClass(prioridade) {
        const classes = {
            alta: 'prioridade-alta',
            media: 'prioridade-media',
            baixa: 'prioridade-baixa'
        };
        return classes[prioridade] || 'prioridade-media';
    }

    setupEventosInteractions() {
        // Configurar drag and drop
        $('.evento-item').attr('draggable', true);
        
        // Configurar clique para detalhes
        $('.evento-item').on('click', (e) => {
            if (!$(e.target).closest('.btn-group').length) {
                const eventoId = $(e.currentTarget).data('evento-id');
                this.showEventDetails(eventoId);
            }
        });
    }

    showEventDetails(eventoId, eventProps = null) {
        if (eventProps) {
            this.renderEventDetailsModal(eventProps);
        } else {
            $.ajax({
                url: `${AgendaConfig.baseUrl}/eventos/${eventoId}`,
                type: 'GET',
                success: (evento) => {
                    this.renderEventDetailsModal(evento);
                },
                error: (xhr) => {
                    console.error('Erro ao carregar detalhes do evento:', xhr.responseText);
                    this.showError('Erro ao carregar detalhes do evento');
                }
            });
        }
    }

    renderEventDetailsModal(evento) {
        const modalHtml = `
            <div class="modal fade agenda-modal" id="modalDetalhesEvento" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">
                                <i class="fas fa-calendar-alt me-2"></i>${evento.titulo}
                            </h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <h6><i class="fas fa-info-circle me-2"></i>Informações Gerais</h6>
                                    <table class="table table-sm">
                                        <tr>
                                            <td><strong>Data/Hora:</strong></td>
                                            <td>${moment(evento.dataInicio).format(AgendaConfig.dateTimeFormat)}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>Tipo:</strong></td>
                                            <td><span class="badge bg-primary">${evento.tipo}</span></td>
                                        </tr>
                                        <tr>
                                            <td><strong>Status:</strong></td>
                                            <td><span class="agenda-badge ${this.getStatusClass(evento.status)}">${evento.status}</span></td>
                                        </tr>
                                        <tr>
                                            <td><strong>Prioridade:</strong></td>
                                            <td><span class="agenda-badge ${this.getPrioridadeClass(evento.prioridade)}">${evento.prioridade}</span></td>
                                        </tr>
                                        ${evento.local ? `
                                            <tr>
                                                <td><strong>Local:</strong></td>
                                                <td>${evento.local}</td>
                                            </tr>
                                        ` : ''}
                                    </table>
                                </div>
                                <div class="col-md-6">
                                    ${evento.participantes && evento.participantes.length > 0 ? `
                                        <h6><i class="fas fa-users me-2"></i>Participantes</h6>
                                        <ul class="list-unstyled">
                                            ${evento.participantes.map(p => `<li><i class="fas fa-user me-1"></i>${p.nome}</li>`).join('')}
                                        </ul>
                                    ` : ''}
                                    ${evento.anexos && evento.anexos.length > 0 ? `
                                        <h6><i class="fas fa-paperclip me-2"></i>Anexos</h6>
                                        <ul class="list-unstyled">
                                            ${evento.anexos.map(a => `<li><a href="${a.url}" target="_blank"><i class="fas fa-file me-1"></i>${a.nome}</a></li>`).join('')}
                                        </ul>
                                    ` : ''}
                                </div>
                            </div>
                            ${evento.descricao ? `
                                <div class="row mt-3">
                                    <div class="col-12">
                                        <h6><i class="fas fa-align-left me-2"></i>Descrição</h6>
                                        <p class="text-muted">${evento.descricao}</p>
                                    </div>
                                </div>
                            ` : ''}
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">
                                <i class="fas fa-times me-1"></i>Fechar
                            </button>
                            <button type="button" class="btn btn-primary" onclick="agendaManager.editEvento(${evento.id})">
                                <i class="fas fa-edit me-1"></i>Editar
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        // Remover modal anterior se existir
        $('#modalDetalhesEvento').remove();
        
        // Adicionar novo modal
        $('body').append(modalHtml);
        
        // Mostrar modal
        const modal = new bootstrap.Modal(document.getElementById('modalDetalhesEvento'));
        modal.show();
    }

    editEvento(eventoId) {
        window.location.href = `${AgendaConfig.baseUrl}/eventos/${eventoId}/editar`;
    }

    duplicateEvento(eventoId) {
        if (!confirm('Deseja duplicar este evento?')) return;
        
        $.ajax({
            url: `${AgendaConfig.baseUrl}/eventos/${eventoId}/duplicar`,
            type: 'POST',
            success: (data) => {
                this.showSuccess('Evento duplicado com sucesso!');
                this.loadEventos();
                if (this.calendar) {
                    this.calendar.refetchEvents();
                }
            },
            error: (xhr) => {
                console.error('Erro ao duplicar evento:', xhr.responseText);
                this.showError('Erro ao duplicar evento');
            }
        });
    }

    deleteEvento(eventoId) {
        if (!confirm('Tem certeza que deseja excluir este evento?')) return;
        
        $.ajax({
            url: `${AgendaConfig.baseUrl}/eventos/${eventoId}`,
            type: 'DELETE',
            success: () => {
                this.showSuccess('Evento excluído com sucesso!');
                this.loadEventos();
                if (this.calendar) {
                    this.calendar.refetchEvents();
                }
            },
            error: (xhr) => {
                console.error('Erro ao excluir evento:', xhr.responseText);
                this.showError('Erro ao excluir evento');
            }
        });
    }

    updateEventDateTime(eventoId, dateTime) {
        $.ajax({
            url: `${AgendaConfig.baseUrl}/eventos/${eventoId}/datetime`,
            type: 'PATCH',
            contentType: 'application/json',
            data: JSON.stringify(dateTime),
            success: () => {
                this.showSuccess('Evento atualizado com sucesso!');
                this.loadEventos();
            },
            error: (xhr) => {
                console.error('Erro ao atualizar evento:', xhr.responseText);
                this.showError('Erro ao atualizar evento');
                if (this.calendar) {
                    this.calendar.refetchEvents();
                }
            }
        });
    }

    handleFormSubmit(e) {
        e.preventDefault();
        
        const form = $(e.target);
        const formData = new FormData(form[0]);
        
        // Validar formulário
        if (!this.validateForm(form)) {
            return false;
        }
        
        // Mostrar loading
        const submitBtn = form.find('button[type="submit"]');
        const originalText = submitBtn.html();
        submitBtn.html('<i class="fas fa-spinner fa-spin me-1"></i>Salvando...').prop('disabled', true);
        
        $.ajax({
            url: form.attr('action'),
            type: form.attr('method') || 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: (data) => {
                this.showSuccess('Dados salvos com sucesso!');
                this.loadEventos();
                if (this.calendar) {
                    this.calendar.refetchEvents();
                }
                
                // Fechar modal se existir
                const modal = form.closest('.modal');
                if (modal.length) {
                    bootstrap.Modal.getInstance(modal[0])?.hide();
                }
                
                // Redirecionar se necessário
                if (data.redirect) {
                    window.location.href = data.redirect;
                }
            },
            error: (xhr) => {
                console.error('Erro ao salvar:', xhr.responseText);
                this.showError('Erro ao salvar dados');
                
                // Mostrar erros de validação
                if (xhr.status === 422 && xhr.responseJSON?.errors) {
                    this.showValidationErrors(form, xhr.responseJSON.errors);
                }
            },
            complete: () => {
                submitBtn.html(originalText).prop('disabled', false);
            }
        });
    }

    validateForm(form) {
        let isValid = true;
        
        // Limpar erros anteriores
        form.find('.is-invalid').removeClass('is-invalid');
        form.find('.invalid-feedback').remove();
        
        // Validar campos obrigatórios
        form.find('[required]').each((index, element) => {
            const field = $(element);
            if (!field.val() || field.val().trim() === '') {
                this.showFieldError(field, 'Este campo é obrigatório');
                isValid = false;
            }
        });
        
        // Validar datas
        const dataInicio = form.find('#dataInicio').val();
        const dataFim = form.find('#dataFim').val();
        
        if (dataInicio && dataFim) {
            if (moment(dataInicio).isAfter(moment(dataFim))) {
                this.showFieldError(form.find('#dataFim'), 'Data de fim deve ser posterior à data de início');
                isValid = false;
            }
        }
        
        // Validar email
        form.find('input[type="email"]').each((index, element) => {
            const field = $(element);
            const email = field.val();
            if (email && !this.isValidEmail(email)) {
                this.showFieldError(field, 'Email inválido');
                isValid = false;
            }
        });
        
        return isValid;
    }

    showFieldError(field, message) {
        field.addClass('is-invalid');
        field.after(`<div class="invalid-feedback">${message}</div>`);
    }

    showValidationErrors(form, errors) {
        Object.keys(errors).forEach(fieldName => {
            const field = form.find(`[name="${fieldName}"]`);
            if (field.length) {
                this.showFieldError(field, errors[fieldName][0]);
            }
        });
    }

    isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    saveDraft(e) {
        e.preventDefault();
        
        const form = $(e.target).closest('form');
        const formData = new FormData(form[0]);
        formData.append('draft', 'true');
        
        $.ajax({
            url: form.attr('action'),
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: () => {
                this.showInfo('Rascunho salvo!');
            },
            error: (xhr) => {
                console.error('Erro ao salvar rascunho:', xhr.responseText);
                this.showError('Erro ao salvar rascunho');
            }
        });
    }

    applyFilters() {
        this.filtros = {};
        
        $('.filtro-agenda').each((index, element) => {
            const field = $(element);
            const value = field.val();
            if (value && value !== '') {
                this.filtros[field.attr('name')] = value;
            }
        });
        
        this.loadEventos();
        if (this.calendar) {
            this.calendar.refetchEvents();
        }
    }

    clearFilters() {
        $('.filtro-agenda').val('');
        this.filtros = {};
        this.loadEventos();
        if (this.calendar) {
            this.calendar.refetchEvents();
        }
    }

    searchEventos(e) {
        const query = $(e.target).val();
        
        if (query.length >= 3 || query.length === 0) {
            this.filtros.search = query;
            this.loadEventos();
        }
    }

    setupAutoSave() {
        if (!AgendaConfig.autoSave) return;
        
        $(document).on('input change', '.auto-save', this.debounce(() => {
            this.autoSaveForm();
        }, AgendaConfig.autoSaveInterval));
    }

    autoSaveForm() {
        const form = $('.auto-save').closest('form');
        if (!form.length) return;
        
        const formData = new FormData(form[0]);
        formData.append('auto_save', 'true');
        
        $.ajax({
            url: form.attr('action'),
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: () => {
                this.showAutoSaveIndicator();
            },
            error: (xhr) => {
                console.error('Erro no auto-save:', xhr.responseText);
            }
        });
    }

    showAutoSaveIndicator() {
        const indicator = $('.auto-save-indicator');
        if (indicator.length) {
            indicator.text('Salvo automaticamente').fadeIn().delay(2000).fadeOut();
        }
    }

    setupNotifications() {
        // Verificar permissão para notificações
        if ('Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission();
        }
        
        // Verificar lembretes pendentes a cada minuto
        setInterval(() => {
            this.checkPendingReminders();
        }, 60000);
    }

    checkPendingReminders() {
        const now = moment();
        
        this.lembretes.forEach(lembrete => {
            const reminderTime = moment(lembrete.dataHora);
            
            if (reminderTime.isSameOrBefore(now) && !lembrete.notificado) {
                this.showNotification(lembrete);
                this.markReminderAsNotified(lembrete.id);
            }
        });
    }

    showNotification(lembrete) {
        // Notificação do navegador
        if ('Notification' in window && Notification.permission === 'granted') {
            new Notification(lembrete.titulo, {
                body: lembrete.descricao,
                icon: '/images/agenda-icon.png',
                tag: `lembrete-${lembrete.id}`
            });
        }
        
        // Notificação na interface
        this.showAlert(lembrete.titulo, lembrete.descricao, 'info');
    }

    markReminderAsNotified(lembreteId) {
        $.ajax({
            url: `${AgendaConfig.baseUrl}/lembretes/${lembreteId}/notificar`,
            type: 'PATCH',
            success: () => {
                // Atualizar estado local
                const lembrete = this.lembretes.find(l => l.id === lembreteId);
                if (lembrete) {
                    lembrete.notificado = true;
                }
            },
            error: (xhr) => {
                console.error('Erro ao marcar lembrete como notificado:', xhr.responseText);
            }
        });
    }

    handleKeyboardShortcuts(e) {
        // Ctrl/Cmd + N: Novo evento
        if ((e.ctrlKey || e.metaKey) && e.key === 'n') {
            e.preventDefault();
            this.openNewEventModal();
        }
        
        // Ctrl/Cmd + S: Salvar formulário
        if ((e.ctrlKey || e.metaKey) && e.key === 's') {
            const form = $('form:visible').first();
            if (form.length) {
                e.preventDefault();
                form.submit();
            }
        }
        
        // Escape: Fechar modal
        if (e.key === 'Escape') {
            const modal = $('.modal:visible').last();
            if (modal.length) {
                bootstrap.Modal.getInstance(modal[0])?.hide();
            }
        }
    }

    openNewEventModal() {
        const modal = new bootstrap.Modal(document.getElementById('modalNovoEvento'));
        modal.show();
    }

    handleExport(e) {
        e.preventDefault();
        
        const format = $(e.target).data('format') || 'pdf';
        const params = new URLSearchParams({
            ...this.filtros,
            format: format
        });
        
        window.open(`${AgendaConfig.baseUrl}/export?${params.toString()}`, '_blank');
    }

    toggleTimeFields(enabled) {
        const timeFields = $('#horaInicio, #horaFim');
        if (enabled) {
            timeFields.prop('disabled', false).closest('.form-group').show();
        } else {
            timeFields.prop('disabled', true).closest('.form-group').hide();
        }
    }

    updateStatistics() {
        const stats = this.calculateStatistics();
        
        $('#totalEventos').text(stats.total);
        $('#eventosHoje').text(stats.hoje);
        $('#eventosSemana').text(stats.semana);
        $('#eventosMes').text(stats.mes);
        $('#eventosConcluidos').text(stats.concluidos);
        $('#eventosPendentes').text(stats.pendentes);
    }

    calculateStatistics() {
        const now = moment();
        const startOfDay = moment().startOf('day');
        const endOfDay = moment().endOf('day');
        const startOfWeek = moment().startOf('week');
        const endOfWeek = moment().endOf('week');
        const startOfMonth = moment().startOf('month');
        const endOfMonth = moment().endOf('month');
        
        return {
            total: this.eventos.length,
            hoje: this.eventos.filter(e => moment(e.dataInicio).isBetween(startOfDay, endOfDay)).length,
            semana: this.eventos.filter(e => moment(e.dataInicio).isBetween(startOfWeek, endOfWeek)).length,
            mes: this.eventos.filter(e => moment(e.dataInicio).isBetween(startOfMonth, endOfMonth)).length,
            concluidos: this.eventos.filter(e => e.status === 'concluido').length,
            pendentes: this.eventos.filter(e => e.status === 'agendado').length
        };
    }

    // Drag and Drop handlers
    onDragStart(e) {
        const eventoId = $(e.target).data('evento-id');
        e.originalEvent.dataTransfer.setData('text/plain', eventoId);
    }

    onDragOver(e) {
        e.preventDefault();
        $(e.target).addClass('drag-over');
    }

    onDrop(e) {
        e.preventDefault();
        $(e.target).removeClass('drag-over');
        
        const eventoId = e.originalEvent.dataTransfer.getData('text/plain');
        const newDate = $(e.target).data('date');
        
        if (eventoId && newDate) {
            this.moveEventToDate(eventoId, newDate);
        }
    }

    moveEventToDate(eventoId, newDate) {
        $.ajax({
            url: `${AgendaConfig.baseUrl}/eventos/${eventoId}/move`,
            type: 'PATCH',
            contentType: 'application/json',
            data: JSON.stringify({ date: newDate }),
            success: () => {
                this.showSuccess('Evento movido com sucesso!');
                this.loadEventos();
                if (this.calendar) {
                    this.calendar.refetchEvents();
                }
            },
            error: (xhr) => {
                console.error('Erro ao mover evento:', xhr.responseText);
                this.showError('Erro ao mover evento');
            }
        });
    }

    // Modal handlers
    onModalShow(e) {
        const modal = $(e.target);
        
        // Focar no primeiro campo
        setTimeout(() => {
            modal.find('input, select, textarea').first().focus();
        }, 150);
        
        // Configurar validação em tempo real
        modal.find('form').on('input change', 'input, select, textarea', (event) => {
            this.validateField($(event.target));
        });
    }

    onModalHide(e) {
        const modal = $(e.target);
        
        // Limpar formulário
        modal.find('form')[0]?.reset();
        
        // Limpar erros de validação
        modal.find('.is-invalid').removeClass('is-invalid');
        modal.find('.invalid-feedback').remove();
    }

    validateField(field) {
        field.removeClass('is-invalid');
        field.siblings('.invalid-feedback').remove();
        
        if (field.prop('required') && !field.val()) {
            this.showFieldError(field, 'Este campo é obrigatório');
            return false;
        }
        
        if (field.attr('type') === 'email' && field.val() && !this.isValidEmail(field.val())) {
            this.showFieldError(field, 'Email inválido');
            return false;
        }
        
        return true;
    }

    // Utility functions
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    showLoading() {
        if (!$('#loadingOverlay').length) {
            $('body').append(`
                <div id="loadingOverlay" class="position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center" style="background: rgba(0,0,0,0.5); z-index: 9999;">
                    <div class="text-center text-white">
                        <div class="spinner-border mb-3" role="status"></div>
                        <div>Carregando...</div>
                    </div>
                </div>
            `);
        }
    }

    hideLoading() {
        $('#loadingOverlay').remove();
    }

    showSuccess(message) {
        this.showAlert('Sucesso!', message, 'success');
    }

    showError(message) {
        this.showAlert('Erro!', message, 'danger');
    }

    showInfo(message) {
        this.showAlert('Informação', message, 'info');
    }

    showAlert(title, message, type = 'info') {
        const alertId = 'alert-' + Date.now();
        const alertHtml = `
            <div id="${alertId}" class="alert alert-${type} alert-dismissible fade show position-fixed" style="top: 20px; right: 20px; z-index: 9999; min-width: 300px;">
                <strong>${title}</strong> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
        
        $('body').append(alertHtml);
        
        // Auto-remover após 5 segundos
        setTimeout(() => {
            $(`#${alertId}`).alert('close');
        }, 5000);
    }

    // Métodos públicos para uso externo
    refreshCalendar() {
        if (this.calendar) {
            this.calendar.refetchEvents();
        }
    }

    refreshEventos() {
        this.loadEventos();
    }

    refreshLembretes() {
        this.loadLembretes();
    }

    getEventos() {
        return this.eventos;
    }

    getLembretes() {
        return this.lembretes;
    }

    getFiltros() {
        return this.filtros;
    }
}

// Inicializar quando o documento estiver pronto
$(document).ready(function() {
    // Configurar moment.js para português
    if (typeof moment !== 'undefined') {
        moment.locale('pt-br');
    }
    
    // Inicializar gerenciador da agenda
    window.agendaManager = new AgendaManager();
    
    // Configurar tooltips globais
    $('[data-bs-toggle="tooltip"]').tooltip();
    
    // Configurar popovers globais
    $('[data-bs-toggle="popover"]').popover();
    
    // Configurar date pickers
    $('.datepicker').each(function() {
        $(this).datepicker({
            format: 'dd/mm/yyyy',
            language: 'pt-BR',
            autoclose: true,
            todayHighlight: true
        });
    });
    
    // Configurar time pickers
    $('.timepicker').each(function() {
        $(this).timepicker({
            timeFormat: 'HH:mm',
            interval: 15,
            minTime: '00:00',
            maxTime: '23:59',
            defaultTime: '09:00',
            startTime: '00:00',
            dynamic: false,
            dropdown: true,
            scrollbar: true
        });
    });
    
    // Configurar máscaras de input
    $('.phone-mask').mask('(00) 00000-0000');
    $('.cpf-mask').mask('000.000.000-00');
    $('.cnpj-mask').mask('00.000.000/0000-00');
    $('.cep-mask').mask('00000-000');
    
    // Configurar upload de arquivos
    $('.file-upload').on('change', function() {
        const file = this.files[0];
        if (file) {
            // Validar tamanho
            if (file.size > AgendaConfig.maxFileSize) {
                alert('Arquivo muito grande. Tamanho máximo: 5MB');
                $(this).val('');
                return;
            }
            
            // Validar tipo
            const extension = file.name.split('.').pop().toLowerCase();
            if (!AgendaConfig.allowedFileTypes.includes(extension)) {
                alert('Tipo de arquivo não permitido.');
                $(this).val('');
                return;
            }
            
            // Mostrar nome do arquivo
            $(this).siblings('.file-name').text(file.name);
        }
    });
    
    // Configurar checkbox "Dia Inteiro"
    $(document).on('change', '#diaInteiro', function() {
        agendaManager.toggleTimeFields(!this.checked);
    });
    
    // Configurar busca em tempo real
    $('.search-input').on('input', agendaManager.debounce(function() {
        const query = $(this).val();
        agendaManager.filtros.search = query;
        agendaManager.loadEventos();
    }, 300));
});

// Funções globais para compatibilidade
function refreshAgenda() {
    if (window.agendaManager) {
        window.agendaManager.refreshCalendar();
        window.agendaManager.refreshEventos();
    }
}

function showEventDetails(eventoId) {
    if (window.agendaManager) {
        window.agendaManager.showEventDetails(eventoId);
    }
}

function editEvento(eventoId) {
    if (window.agendaManager) {
        window.agendaManager.editEvento(eventoId);
    }
}

function deleteEvento(eventoId) {
    if (window.agendaManager) {
        window.agendaManager.deleteEvento(eventoId);
    }
}

function duplicateEvento(eventoId) {
    if (window.agendaManager) {
        window.agendaManager.duplicateEvento(eventoId);
    }
}

// Exportar para uso em módulos
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { AgendaManager, AgendaConfig };
}