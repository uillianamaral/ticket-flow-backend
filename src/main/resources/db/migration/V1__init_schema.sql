-- ==============================================================================
-- TicketFlow — V1__init_schema.sql
-- Migração inicial Flyway com todas as tabelas e índices da arquitetura
-- ==============================================================================

-- 1. Sequência para geração padronizada de protocolos (SUP-000001)
CREATE SEQUENCE IF NOT EXISTS ticket_protocol_seq START WITH 1 INCREMENT BY 1;

-- 2. Tabela de Usuários
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL, -- SOLICITANTE, ATENDENTE, ADMIN
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabela de Equipes de Atendimento
CREATE TABLE IF NOT EXISTS teams (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 4. Associação Usuário x Equipe
CREATE TABLE IF NOT EXISTS team_members (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_team_member UNIQUE (team_id, user_id)
);

-- 5. Categorias de Atendimento
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    default_team_id UUID NOT NULL REFERENCES teams(id),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 6. Políticas de SLA por Prioridade
CREATE TABLE IF NOT EXISTS sla_policies (
    id UUID PRIMARY KEY,
    priority VARCHAR(50) NOT NULL UNIQUE, -- BAIXA, MEDIA, ALTA, CRITICA
    first_response_hours INT NOT NULL,
    resolution_hours INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 7. Tabela Principal de Chamados (Tickets)
CREATE TABLE IF NOT EXISTS tickets (
    id UUID PRIMARY KEY,
    protocol VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category_id UUID NOT NULL REFERENCES categories(id),
    priority VARCHAR(50) NOT NULL, -- BAIXA, MEDIA, ALTA, CRITICA
    status VARCHAR(50) NOT NULL DEFAULT 'ABERTO', -- ABERTO, EM_ATENDIMENTO, AGUARDANDO_SOLICITANTE, RESOLVIDO, ENCERRADO
    requester_id UUID NOT NULL REFERENCES users(id),
    assigned_team_id UUID REFERENCES teams(id),
    assigned_user_id UUID REFERENCES users(id),
    solution TEXT,
    version INT NOT NULL DEFAULT 0,
    first_response_due_at TIMESTAMP WITH TIME ZONE,
    resolution_due_at TIMESTAMP WITH TIME ZONE,
    first_response_at TIMESTAMP WITH TIME ZONE,
    resolved_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. Mensagens e Notas Internas
CREATE TABLE IF NOT EXISTS ticket_comments (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    is_internal BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 9. Anexos do Chamado
CREATE TABLE IF NOT EXISTS ticket_attachments (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    uploaded_by_id UUID NOT NULL REFERENCES users(id),
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 10. Histórico de Auditoria Transacional
CREATE TABLE IF NOT EXISTS ticket_history (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    actor_id UUID NOT NULL REFERENCES users(id),
    field_changed VARCHAR(100) NOT NULL,
    old_value VARCHAR(255),
    new_value VARCHAR(255),
    reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 11. Central de Notificações
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ticket_id UUID REFERENCES tickets(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices de Performance
CREATE INDEX IF NOT EXISTS idx_tickets_requester ON tickets(requester_id);
CREATE INDEX IF NOT EXISTS idx_tickets_assigned_team ON tickets(assigned_team_id);
CREATE INDEX IF NOT EXISTS idx_tickets_assigned_user ON tickets(assigned_user_id);
CREATE INDEX IF NOT EXISTS idx_tickets_status ON tickets(status);
CREATE INDEX IF NOT EXISTS idx_tickets_created_at ON tickets(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_ticket_comments_ticket ON ticket_comments(ticket_id);
CREATE INDEX IF NOT EXISTS idx_ticket_comments_internal ON ticket_comments(ticket_id, is_internal);
CREATE INDEX IF NOT EXISTS idx_ticket_attachments_ticket ON ticket_attachments(ticket_id);
CREATE INDEX IF NOT EXISTS idx_ticket_history_ticket ON ticket_history(ticket_id);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications(recipient_id, is_read);
