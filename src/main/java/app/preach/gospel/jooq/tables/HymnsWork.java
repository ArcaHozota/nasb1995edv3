/*
 * This file is generated by jOOQ.
 */
package app.preach.gospel.jooq.tables;


import app.preach.gospel.jooq.Indexes;
import app.preach.gospel.jooq.Keys;
import app.preach.gospel.jooq.Public;
import app.preach.gospel.jooq.tables.records.HymnsWorkRecord;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function6;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row6;
import org.jooq.Schema;
import org.jooq.SelectField;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class HymnsWork extends TableImpl<HymnsWorkRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.hymns_work</code>
     */
    public static final HymnsWork HYMNS_WORK = new HymnsWork();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<HymnsWorkRecord> getRecordType() {
        return HymnsWorkRecord.class;
    }

    /**
     * The column <code>public.hymns_work.id</code>. ID
     */
    public final TableField<HymnsWorkRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "ID");

    /**
     * The column <code>public.hymns_work.work_id</code>. ワークID
     */
    public final TableField<HymnsWorkRecord, Long> WORK_ID = createField(DSL.name("work_id"), SQLDataType.BIGINT.nullable(false), this, "ワークID");

    /**
     * The column <code>public.hymns_work.score</code>. 楽譜
     */
    public final TableField<HymnsWorkRecord, byte[]> SCORE = createField(DSL.name("score"), SQLDataType.BLOB, this, "楽譜");

    /**
     * The column <code>public.hymns_work.name_jp_rational</code>. 日本語名称
     */
    public final TableField<HymnsWorkRecord, String> NAME_JP_RATIONAL = createField(DSL.name("name_jp_rational"), SQLDataType.VARCHAR(120), this, "日本語名称");

    /**
     * The column <code>public.hymns_work.updated_time</code>. 更新時間
     */
    public final TableField<HymnsWorkRecord, OffsetDateTime> UPDATED_TIME = createField(DSL.name("updated_time"), SQLDataType.TIMESTAMPWITHTIMEZONE(6).nullable(false), this, "更新時間");

    /**
     * The column <code>public.hymns_work.biko</code>. 備考
     */
    public final TableField<HymnsWorkRecord, String> BIKO = createField(DSL.name("biko"), SQLDataType.VARCHAR(15), this, "備考");

    private HymnsWork(Name alias, Table<HymnsWorkRecord> aliased) {
        this(alias, aliased, null);
    }

    private HymnsWork(Name alias, Table<HymnsWorkRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.hymns_work</code> table reference
     */
    public HymnsWork(String alias) {
        this(DSL.name(alias), HYMNS_WORK);
    }

    /**
     * Create an aliased <code>public.hymns_work</code> table reference
     */
    public HymnsWork(Name alias) {
        this(alias, HYMNS_WORK);
    }

    /**
     * Create a <code>public.hymns_work</code> table reference
     */
    public HymnsWork() {
        this(DSL.name("hymns_work"), null);
    }

    public <O extends Record> HymnsWork(Table<O> child, ForeignKey<O, HymnsWorkRecord> key) {
        super(child, key, HYMNS_WORK);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.HYMNS_WORK_WORK_ID_KEY);
    }

    @Override
    public Identity<HymnsWorkRecord, Long> getIdentity() {
        return (Identity<HymnsWorkRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<HymnsWorkRecord> getPrimaryKey() {
        return Keys.HYMNS_WORK_PKEY;
    }

    @Override
    public List<ForeignKey<HymnsWorkRecord, ?>> getReferences() {
        return Arrays.asList(Keys.HYMNS_WORK__HYMNS_WORK_HYMNS_TO_WORK);
    }

    private transient Hymns _hymns;

    /**
     * Get the implicit join path to the <code>public.hymns</code> table.
     */
    public Hymns hymns() {
        if (_hymns == null)
            _hymns = new Hymns(this, Keys.HYMNS_WORK__HYMNS_WORK_HYMNS_TO_WORK);

        return _hymns;
    }

    @Override
    public HymnsWork as(String alias) {
        return new HymnsWork(DSL.name(alias), this);
    }

    @Override
    public HymnsWork as(Name alias) {
        return new HymnsWork(alias, this);
    }

    @Override
    public HymnsWork as(Table<?> alias) {
        return new HymnsWork(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public HymnsWork rename(String name) {
        return new HymnsWork(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public HymnsWork rename(Name name) {
        return new HymnsWork(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public HymnsWork rename(Table<?> name) {
        return new HymnsWork(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row6 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row6<Long, Long, byte[], String, OffsetDateTime, String> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function6<? super Long, ? super Long, ? super byte[], ? super String, ? super OffsetDateTime, ? super String, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function6<? super Long, ? super Long, ? super byte[], ? super String, ? super OffsetDateTime, ? super String, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
