/*
 * This file is generated by jOOQ.
 */
package app.preach.gospel.jooq;


import app.preach.gospel.jooq.tables.Authorities;
import app.preach.gospel.jooq.tables.Books;
import app.preach.gospel.jooq.tables.Chapters;
import app.preach.gospel.jooq.tables.Hymns;
import app.preach.gospel.jooq.tables.HymnsWork;
import app.preach.gospel.jooq.tables.Phrases;
import app.preach.gospel.jooq.tables.RoleAuth;
import app.preach.gospel.jooq.tables.Roles;
import app.preach.gospel.jooq.tables.Students;
import app.preach.gospel.jooq.tables.records.AuthoritiesRecord;
import app.preach.gospel.jooq.tables.records.BooksRecord;
import app.preach.gospel.jooq.tables.records.ChaptersRecord;
import app.preach.gospel.jooq.tables.records.HymnsRecord;
import app.preach.gospel.jooq.tables.records.HymnsWorkRecord;
import app.preach.gospel.jooq.tables.records.PhrasesRecord;
import app.preach.gospel.jooq.tables.records.RoleAuthRecord;
import app.preach.gospel.jooq.tables.records.RolesRecord;
import app.preach.gospel.jooq.tables.records.StudentsRecord;

import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in
 * public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<AuthoritiesRecord> AUTH_NAME_UNIQUE = Internal.createUniqueKey(Authorities.AUTHORITIES, DSL.name("auth_name_unique"), new TableField[] { Authorities.AUTHORITIES.NAME }, true);
    public static final UniqueKey<AuthoritiesRecord> AUTH_PKEY = Internal.createUniqueKey(Authorities.AUTHORITIES, DSL.name("auth_pkey"), new TableField[] { Authorities.AUTHORITIES.ID }, true);
    public static final UniqueKey<AuthoritiesRecord> AUTH_TITLE_UNIQUE = Internal.createUniqueKey(Authorities.AUTHORITIES, DSL.name("auth_title_unique"), new TableField[] { Authorities.AUTHORITIES.TITLE }, true);
    public static final UniqueKey<BooksRecord> BOOKS_PKEY = Internal.createUniqueKey(Books.BOOKS, DSL.name("books_pkey"), new TableField[] { Books.BOOKS.ID }, true);
    public static final UniqueKey<ChaptersRecord> CHAPTERS_PKEY = Internal.createUniqueKey(Chapters.CHAPTERS, DSL.name("chapters_pkey"), new TableField[] { Chapters.CHAPTERS.ID }, true);
    public static final UniqueKey<HymnsRecord> HYMNS_PKEY = Internal.createUniqueKey(Hymns.HYMNS, DSL.name("hymns_pkey"), new TableField[] { Hymns.HYMNS.ID }, true);
    public static final UniqueKey<HymnsWorkRecord> HYMNS_WORK_PKEY = Internal.createUniqueKey(HymnsWork.HYMNS_WORK, DSL.name("hymns_work_pkey"), new TableField[] { HymnsWork.HYMNS_WORK.ID }, true);
    public static final UniqueKey<PhrasesRecord> PHRASES_PKEY = Internal.createUniqueKey(Phrases.PHRASES, DSL.name("phrases_pkey"), new TableField[] { Phrases.PHRASES.ID }, true);
    public static final UniqueKey<RoleAuthRecord> ROLE_AUTH_PKEY = Internal.createUniqueKey(RoleAuth.ROLE_AUTH, DSL.name("role_auth_pkey"), new TableField[] { RoleAuth.ROLE_AUTH.ROLE_ID, RoleAuth.ROLE_AUTH.AUTH_ID }, true);
    public static final UniqueKey<RolesRecord> ROLE_PKEY = Internal.createUniqueKey(Roles.ROLES, DSL.name("role_pkey"), new TableField[] { Roles.ROLES.ID }, true);
    public static final UniqueKey<StudentsRecord> STUDENT_PKEY = Internal.createUniqueKey(Students.STUDENTS, DSL.name("student_pkey"), new TableField[] { Students.STUDENTS.ID }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<ChaptersRecord, BooksRecord> CHAPTERS__CHAPTERS_BOOKS_TO_CHAPTER = Internal.createForeignKey(Chapters.CHAPTERS, DSL.name("chapters_books_to_chapter"), new TableField[] { Chapters.CHAPTERS.BOOK_ID }, Keys.BOOKS_PKEY, new TableField[] { Books.BOOKS.ID }, true);
    public static final ForeignKey<HymnsRecord, StudentsRecord> HYMNS__HYMNS_STUDENTS_UPDATED_HYMNS = Internal.createForeignKey(Hymns.HYMNS, DSL.name("hymns_students_updated_hymns"), new TableField[] { Hymns.HYMNS.UPDATED_USER }, Keys.STUDENT_PKEY, new TableField[] { Students.STUDENTS.ID }, true);
    public static final ForeignKey<HymnsWorkRecord, HymnsRecord> HYMNS_WORK__HYMNS_WORK_HYMNS_TO_WORK = Internal.createForeignKey(HymnsWork.HYMNS_WORK, DSL.name("hymns_work_hymns_to_work"), new TableField[] { HymnsWork.HYMNS_WORK.WORK_ID }, Keys.HYMNS_PKEY, new TableField[] { Hymns.HYMNS.ID }, true);
    public static final ForeignKey<PhrasesRecord, ChaptersRecord> PHRASES__PHRASES_CHAPTERS_TO_PHRASE = Internal.createForeignKey(Phrases.PHRASES, DSL.name("phrases_chapters_to_phrase"), new TableField[] { Phrases.PHRASES.CHAPTER_ID }, Keys.CHAPTERS_PKEY, new TableField[] { Chapters.CHAPTERS.ID }, true);
    public static final ForeignKey<RoleAuthRecord, AuthoritiesRecord> ROLE_AUTH__ROLE_AUTH_AUTH_ID = Internal.createForeignKey(RoleAuth.ROLE_AUTH, DSL.name("role_auth_auth_id"), new TableField[] { RoleAuth.ROLE_AUTH.AUTH_ID }, Keys.AUTH_PKEY, new TableField[] { Authorities.AUTHORITIES.ID }, true);
    public static final ForeignKey<RoleAuthRecord, RolesRecord> ROLE_AUTH__ROLE_AUTH_ROLE_ID = Internal.createForeignKey(RoleAuth.ROLE_AUTH, DSL.name("role_auth_role_id"), new TableField[] { RoleAuth.ROLE_AUTH.ROLE_ID }, Keys.ROLE_PKEY, new TableField[] { Roles.ROLES.ID }, true);
    public static final ForeignKey<StudentsRecord, RolesRecord> STUDENTS__STUDENTS_ROLES_ROLED = Internal.createForeignKey(Students.STUDENTS, DSL.name("students_roles_roled"), new TableField[] { Students.STUDENTS.ROLE_ID }, Keys.ROLE_PKEY, new TableField[] { Roles.ROLES.ID }, true);
}
