CREATE SEQUENCE IF NOT EXISTS TEMP_ACTION_SEQ;
CREATE SEQUENCE IF NOT EXISTS TEMP_DOCUMENT_SEQ;

CREATE TABLE IF NOT EXISTS TEMP_ACTION (
    SID                  INT8                 not null default nextval('TEMP_ACTION_SEQ'),
    FK_CASE_SID          INT8                 null,
    ID                   VARCHAR(255)         not null,
    NAME                 VARCHAR(255)         null,
    STATUS               VARCHAR(30)          not null default 'NEW',
    DISPLAY_NAME         VARCHAR(255)         null,
    DOCUMENT_TYPE        VARCHAR(255)         null,
    DOCUMENT_ID          VARCHAR(255)         null,
    PARENT_DOCUMENT_ID   VARCHAR(255)         null,
    JSON                 TEXT                 null
);

CREATE TABLE IF NOT EXISTS TEMP_DOCUMENT (
   SID                  INT8                 not null default nextval('TEMP_DOCUMENT_SEQ'),
   FK_CASE_SID          INT8                 not null,
   ID                   VARCHAR(255)         not null,
   STATUS               VARCHAR(20)          null,
   DIRECTION            VARCHAR(255)         null,
   TYPE                 VARCHAR(255)         null,
   IS_STARTER           BOOL                 not null default false,
   PARENT_DOCUMENT_ID   VARCHAR(255)         null,
   JSON                 TEXT                 null
);

ALTER TABLE TEMP_ACTION DROP CONSTRAINT IF EXISTS FK_TEMP_ACTION__CASE;
ALTER TABLE TEMP_DOCUMENT DROP CONSTRAINT IF EXISTS FK_TEMP_DOCUMENT__CASE;

alter table TEMP_ACTION
    add constraint FK_TEMP_ACTION__CASE foreign key (FK_CASE_SID)
        references RINA_CASE (SID)
        on delete restrict on update restrict;

alter table TEMP_DOCUMENT
    add constraint FK_TEMP_DOCUMENT__CASE foreign key (FK_CASE_SID)
        references RINA_CASE (SID)
        on delete restrict on update restrict;
