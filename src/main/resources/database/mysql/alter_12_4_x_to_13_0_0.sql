-- organisation
create table o_org_organisation_type (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description mediumtext,
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_css_class varchar(64),
  primary key (id)
);

alter table o_org_organisation_type ENGINE = InnoDB;

create table o_org_organisation (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description mediumtext,
  o_m_path_keys varchar(255),
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_status varchar(32),
  o_css_class varchar(64),
  fk_group bigint not null,
  fk_root bigint,
  fk_parent bigint,
  fk_type bigint,
  primary key (id)
);

alter table o_org_organisation ENGINE = InnoDB;

alter table o_org_organisation add constraint org_to_group_idx foreign key (fk_group) references o_bs_group (id);
alter table o_org_organisation add constraint org_to_root_org_idx foreign key (fk_root) references o_org_organisation (id);
alter table o_org_organisation add constraint org_to_parent_org_idx foreign key (fk_parent) references o_org_organisation (id);
alter table o_org_organisation add constraint org_to_org_type_idx foreign key (fk_type) references o_org_organisation_type (id);

create table o_org_type_to_type (
  id bigint not null auto_increment,
  fk_type bigint not null,
  fk_allowed_sub_type bigint not null,
  primary key (id)
);
alter table o_org_type_to_type ENGINE = InnoDB;

alter table o_org_type_to_type add constraint org_type_to_type_idx foreign key (fk_type) references o_org_organisation_type (id);
alter table o_org_type_to_type add constraint org_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_org_organisation_type (id);


create table o_re_to_organisation (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  r_master bit default 0,
  fk_entry bigint not null,
  fk_organisation bigint not null,
  primary key (id)
);
alter table o_re_to_organisation ENGINE = InnoDB;

alter table o_re_to_organisation add constraint rel_org_to_re_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_re_to_organisation add constraint rel_org_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);


-- curriculum
create table o_cur_element_type (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description mediumtext,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_css_class varchar(64),
  primary key (id)
);
alter table o_cur_element_type ENGINE = InnoDB;

create table o_cur_curriculum (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description mediumtext,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_status varchar(32),
  c_degree varchar(255),
  fk_group bigint not null,
  fk_organisation bigint,
  primary key (id)
);
alter table o_cur_curriculum ENGINE = InnoDB;

alter table o_cur_curriculum add constraint cur_to_group_idx foreign key (fk_group) references o_bs_group (id);
alter table o_cur_curriculum add constraint cur_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);

create table o_cur_curriculum_element (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  pos bigint,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description mediumtext,
  c_status varchar(32),
  c_begin datetime,
  c_end datetime,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  fk_group bigint not null,
  fk_parent bigint,
  fk_curriculum bigint not null,
  primary key (id)
);
alter table o_cur_curriculum_element ENGINE = InnoDB;

alter table o_cur_curriculum_element add constraint cur_el_to_group_idx foreign key (fk_group) references o_bs_group (id);
alter table o_cur_curriculum_element add constraint cur_el_to_cur_el_idx foreign key (fk_parent) references o_cur_curriculum_element (id);
alter table o_cur_curriculum_element add constraint cur_el_to_cur_idx foreign key (fk_curriculum) references o_cur_curriculum (id);

create table o_cur_element_type_to_type (
  id bigint not null auto_increment,
  fk_type bigint not null,
  fk_allowed_sub_type bigint not null,
  primary key (id)
);
alter table o_cur_element_type_to_type ENGINE = InnoDB;

alter table o_cur_element_type_to_type add constraint cur_type_to_type_idx foreign key (fk_type) references o_cur_element_type (id);
alter table o_cur_element_type_to_type add constraint cur_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_cur_element_type (id);

-- drop policy
alter table o_bs_policy drop foreign key FK9A1C5101E2E76DB;


-- evaluation forms
create table o_eva_form_survey (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_resname varchar(50) not null,
   e_resid bigint not null,
   e_sub_ident varchar(2048),
   fk_form_entry bigint not null,
   primary key (id)
);

create table o_eva_form_participation (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_identifier_type varchar(50) not null,
   e_identifier_key varchar(50) not null,
   e_status varchar(20) not null,
   e_anonymous bit not null,
   fk_executor bigint,
   fk_survey bigint not null,
   primary key (id)
);

alter table o_eva_form_response add column e_no_response bit default 0;

alter table o_eva_form_session modify column fk_form_entry bigint;
alter table o_eva_form_session modify column fk_identity bigint;
alter table o_eva_form_session add column fk_survey bigint;
alter table o_eva_form_session add column fk_participation bigint unique;

alter table o_eva_form_survey ENGINE = InnoDB;
alter table o_eva_form_participation ENGINE = InnoDB;

create unique index idx_eva_surv_ores_idx on o_eva_form_survey (e_resid, e_resname, e_sub_ident(255));

alter table o_eva_form_participation add constraint eva_part_to_surv_idx foreign key (fk_survey) references o_eva_form_survey (id);
create unique index idx_eva_part_ident_idx on o_eva_form_participation (e_identifier_key, e_identifier_type, fk_survey);
create unique index idx_eva_part_executor_idx on o_eva_form_participation (fk_executor, fk_survey);

alter table o_eva_form_session add constraint eva_sess_to_surv_idx foreign key (fk_survey) references o_eva_form_survey (id);
alter table o_eva_form_session add constraint eva_sess_to_part_idx foreign key (fk_participation) references o_eva_form_participation (id);

create index idx_eva_resp_report_idx on o_eva_form_response (fk_session, e_responseidentifier, e_no_response);

