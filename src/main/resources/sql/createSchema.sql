
    create table linshare_allowed_mimetype (
        id int8 not null,
        extensions varchar(255),
        mimetype varchar(255),
        status int4,
        primary key (id)
    );

    create table linshare_contact (
        contact_id int8 not null,
        mail varchar(255) not null,
        primary key (contact_id)
    );

    create table linshare_document (
        document_id int8 not null,
        identifier varchar(255) not null unique,
        name varchar(255) not null,
        creation_date timestamp not null,
        expiration_date timestamp not null,
        type varchar(255),
        encrypted bool,
        shared bool,
        size int8,
        file_comment text,
        owner_id int8,
        thmb_uuid varchar(255),
        primary key (document_id)
    );
	
    create table linshare_parameter (
        parameter_id int8 not null,
        file_size_max int8,
        user_available_size int8,
        active_mimetype bool,
        active_signature bool,
        active_encipherment bool,
        user_expiry_time int4,
        user_expiry_time_unit_id int4,
        custom_logo_url varchar(255),
        default_expiry_time int4,
        default_expiry_time_unit_id int4,
		delete_doc_expiry_time bool default false,
        primary key (parameter_id)
    );

    create table linshare_recipient_favourite (
        id int8 not null,
        user_id int8,
        recipient varchar(255),
        weight int8,
        primary key (id)
    );

    create table linshare_secured_url (
        secured_url_id int8 not null,
        url_path varchar(255) not null,
        alea varchar(255) not null,
        expiration_date timestamp not null,
        password varchar(255),
        sender_id int8,
        primary key (secured_url_id),
        unique (url_path, alea)
    );

    create table linshare_secured_url_documents (
        secured_url_id int8 not null,
        elt int8 not null,
        document_index int4 not null,
        primary key (secured_url_id, document_index)
    );

    create table linshare_secured_url_recipients (
        contact_id int8 not null,
        elt int8 not null,
        contact_index int4 not null,
        primary key (contact_id, contact_index)
    );

    create table linshare_share (
        share_id int8 not null,
        document_id int8,
        sender_id int8,
        recipient_id int8,
        expiration_date timestamp,
        share_active bool,
        downloaded bool,
        comment text,
        primary key (share_id)
    );

    create table linshare_share_expiry_rules (
        parameter_id int8 not null,
        expiry_time int4,
        time_unit_id int4,
        share_size int4,
        size_unit_id int4,
        rule_sort_order int4 not null,
        primary key (parameter_id, rule_sort_order)
    );

    create table linshare_signature (
        signature_id int8 not null,
        identifier varchar(255) not null unique,
        name varchar(255) not null,
        creation_date timestamp not null,
        type varchar(255),
        size int8,
        cert_subjectdn varchar(255),
        cert_issuerdn varchar(255),
        cert_notafter timestamp,
        cert text,
        signer_id int8,
        document_id_fk int8,
        sort_order int4,
        primary key (signature_id)
    );

    create table linshare_user (
        user_id int8 not null,
        user_type_id varchar(255) not null,
        login varchar(255) not null unique,
        first_name varchar(255) not null,
        last_name varchar(255) not null,
        encipherment_key_pass bytea,
        mail varchar(255) not null unique,
        creation_date timestamp,
        role_id int4 not null,
        can_upload bool,
        can_create_guest bool default false,
        password varchar(255),
        locale varchar(255),
        expiry_date timestamp,
        comment text,
        owner_id int8,
        primary key (user_id)
    );

    create table linshare_welcome_texts (
        parameter_id int8 not null,
        welcome_text text,
        user_type_id int4,
        language_id int4
    );

    create table public.linshare_log_entry (
        id int8 not null,
        entry_type varchar(255) not null,
        action_date timestamp not null,
        actor_mail varchar(255) not null,
        actor_firstname varchar(255) not null,
        actor_lastname varchar(255) not null,
        log_action varchar(255) not null,
        description varchar(255),
        file_name varchar(255),
        file_type varchar(255),
        file_size int8,
        target_mail varchar(255),
        target_firstname varchar(255),
        target_lastname varchar(255),
        expiration_date timestamp,
        primary key (id)
    );

    create table public.linshare_version (
        id int8 not null,
        description varchar(255) not null
    );


    create index index_document_name on linshare_document (name);

    create index index_document_owner_id on linshare_document (owner_id);

    create index index_document_identifier on linshare_document (identifier);

    create index index_document_expiration_date on linshare_document (expiration_date);

    alter table linshare_document 
        add constraint FK56846E4C675F9781 
        foreign key (owner_id) 
        references linshare_user;

    create index index_favourite_recipient_id on linshare_recipient_favourite (user_id);

    alter table linshare_recipient_favourite 
        add constraint FK847BEC32FB78E769 
        foreign key (user_id) 
        references linshare_user;

    create index idx_secured_url on linshare_secured_url (url_path, alea);

    create index index_securedurl_sender_id on linshare_secured_url (sender_id);

    alter table linshare_secured_url 
        add constraint FK5391E32C62928BF 
        foreign key (sender_id) 
        references linshare_user;

    alter table linshare_secured_url_documents 
        add constraint FK139F29651FBB6B4E 
        foreign key (secured_url_id) 
        references linshare_secured_url;

    alter table linshare_secured_url_documents 
        add constraint FK139F29659AF607D7 
        foreign key (elt) 
        references linshare_document;

    alter table linshare_secured_url_recipients 
        add constraint FK7C25D06D464C4A4B 
        foreign key (contact_id) 
        references linshare_secured_url;

    alter table linshare_secured_url_recipients 
        add constraint FK7C25D06DE97B80DE 
        foreign key (elt) 
        references linshare_contact;

    create index index_share_document_id on linshare_share (document_id);

    create index index_share_sender_id on linshare_share (sender_id);

    create index index_share_recipient_id on linshare_share (recipient_id);

    create index index_share_expiration_date on linshare_share (expiration_date);

    alter table linshare_share 
        add constraint FK83E1284EB927C5E9 
        foreign key (document_id) 
        references linshare_document;

    alter table linshare_share 
        add constraint FK83E1284E62928BF 
        foreign key (sender_id) 
        references linshare_user;

    alter table linshare_share 
        add constraint FK83E1284E4F9C165B 
        foreign key (recipient_id) 
        references linshare_user;

    alter table linshare_share_expiry_rules 
        add constraint FKFDA1673CA44B78EB 
        foreign key (parameter_id) 
        references linshare_parameter;

    create index index_signature_signer_id on linshare_signature (signer_id);

    alter table linshare_signature 
        add constraint FK81C9A1A74472B3AA 
        foreign key (signer_id) 
        references linshare_user;

    alter table linshare_signature 
        add constraint FK81C9A1A7C0BBD6F 
        foreign key (document_id_fk) 
        references linshare_document;

    create index index_user_last_name on linshare_user (last_name);

    create index index_user_mail on linshare_user (mail);

    create index index_user_login on linshare_user (login);

    create index index_user_first_name on linshare_user (first_name);

    alter table linshare_user 
        add constraint FK56D6C97C675F9781 
        foreign key (owner_id) 
        references linshare_user;

    alter table linshare_welcome_texts 
        add constraint FK36A0C738A44B78EB 
        foreign key (parameter_id) 
        references linshare_parameter;

    create index index_userlog_entry_target_mail on public.linshare_log_entry (target_mail);

    create index index_sharelog_entry_file_name on public.linshare_log_entry (file_name);

    create index index_log_entry_actor_first_name on public.linshare_log_entry (actor_firstname);

    create index index_log_entry_actor_mail on public.linshare_log_entry (actor_mail);

    create index index_log_entry_action_date on public.linshare_log_entry (action_date);

    create index index_log_entry_action on public.linshare_log_entry (log_action);

    create index index_sharelog_entry_target_mail on public.linshare_log_entry (target_mail);

    create index index_log_entry_actor_last_name on public.linshare_log_entry (actor_lastname);

    create index index_filelog_entry_file_name on public.linshare_log_entry (file_name);

    create sequence hibernate_sequence;

    insert into linshare_version (id, description) values (7, 'LinShare version 0.7');
