SET search_path TO rina;

alter table DOCUMENT drop constraint FK_DOC__DOC_BVERSION;
alter table DOCUMENT_HISTORY drop constraint FK_DOC_HIS__DOC_BVERSION;
alter table SUBDOCUMENT drop constraint FK_SUBDOC__SUBDOC_BVERSION;
alter table SUBDOCUMENT_HISTORY drop constraint FK_SUBDOC_HIS__SUBDOC_BVERSION;

DELETE FROM nie_event;
DELETE FROM audit_participant;
DELETE FROM audit_object;
DELETE FROM audit_event;
DELETE FROM nie_listener;
DELETE FROM nie_listener;
DELETE FROM nie_listener;
DELETE FROM nie_subscriber;
DELETE FROM nie_subscription;
DELETE FROM business_exception;
DELETE FROM assignment_request;
DELETE FROM notification_user;
DELETE FROM notification;
DELETE FROM notification_alarm;
DELETE FROM assignment_group;
DELETE FROM assignment_user;
DELETE FROM assignment;
DELETE FROM action_tag;
DELETE FROM action;
DELETE FROM activity;
DELETE FROM case_prefill;
DELETE FROM case_subject_org;
DELETE FROM pending_attachment;
DELETE FROM pending_message;
DELETE FROM pending_signature;
DELETE FROM case_participant;
DELETE FROM signature;
DELETE FROM user_message_response;
DELETE FROM user_message;
DELETE FROM doc_bversion_subdoc_bversion;
DELETE FROM subdoc_bversion_attachment;
DELETE FROM subdocument_prefill;
DELETE FROM subdocument_bversion;
DELETE FROM subdocument_content;
DELETE FROM subdocument_attachment;
DELETE FROM subdocument_history;
DELETE FROM subdocument_attachment;
DELETE FROM subdocument;
DELETE FROM conv_participant;
DELETE FROM document_conversation;
DELETE FROM doc_bversion_attachment;
DELETE FROM document_thumbnail;
DELETE FROM document_bversion;
DELETE FROM document_content;
DELETE FROM document_attachment;
DELETE FROM document_comment;
DELETE FROM document_history;
DELETE FROM document;
DELETE FROM case_participant;
DELETE FROM case_attachment;
DELETE FROM case_comment;
DELETE FROM case_property;
DELETE FROM rina_case;

alter table DOCUMENT add constraint FK_DOC__DOC_BVERSION foreign key (FK_DOC_BVERSION_SID) references DOCUMENT_BVERSION (SID) on delete restrict on update restrict;;
alter table DOCUMENT_HISTORY add constraint FK_DOC_HIS__DOC_BVERSION foreign key (FK_DOC_BVERSION_SID) references DOCUMENT_BVERSION (SID) on delete restrict on update restrict;;
alter table SUBDOCUMENT add constraint FK_SUBDOC__SUBDOC_BVERSION foreign key (FK_SUBDOC_BVERSION_SID) references SUBDOCUMENT_BVERSION (SID) on delete restrict on update restrict;;
alter table SUBDOCUMENT_HISTORY add constraint FK_SUBDOC_HIS__SUBDOC_BVERSION foreign key (FK_SUBDOC_BVERSION_SID) references SUBDOCUMENT_BVERSION (SID) on delete restrict on update restrict;;

DELETE FROM policy;

DELETE FROM assigned_buc;

DELETE FROM rule_country;
DELETE FROM rule_creator_group;
DELETE FROM rule_group;
DELETE FROM rule_process;

DELETE FROM rule_role;
DELETE FROM rule_sector;
DELETE FROM rule_creator_user;
DELETE FROM rule_user;
DELETE FROM rule_organisation;

DELETE FROM ass_pol_ass_pol_target;
DELETE FROM assignment_policy_target;
DELETE FROM assignment_policy_rule;
DELETE FROM assignment_policy_policy;
DELETE FROM assignment_policy;

DELETE FROM field where fk_field_chooser_sid not in (select sid from field_chooser where fk_user_sid = 0);
DELETE FROM field_chooser where fk_user_sid <> '0';

DELETE FROM search_def_user;
DELETE FROM search_def_group;
DELETE FROM search_def_org;
DELETE FROM search_def_proc_def;
DELETE from search_definition;

DELETE FROM user_profile;
DELETE FROM iam_user_group;
DELETE FROM iam_group;
DELETE FROM iam_user where id <> '0';
DELETE FROM iam_origin where sid <> 1;

DELETE FROM archiving_volume;

DELETE FROM tenant_param;
DELETE FROM tenant_param_group;
DELETE FROM tenant;

DELETE FROM org_contact_method;
DELETE FROM organisation;

DELETE FROM cluster_node;
DELETE FROM business_key;

DELETE FROM check_instance;
DELETE FROM check_bucket;

DELETE FROM resource;
