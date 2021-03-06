package com.orientechnologies.orient.test.database.auto;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * @author Matan Shukry (matanshukry@gmail.com)
 * @since 3/3/2015
 */
public class DefaultValuesTrivialTest {
  private static final int                      DOCUMENT_COUNT = 50;
  private final OPartitionedDatabasePoolFactory poolFactory    = new OPartitionedDatabasePoolFactory();

  @Test
  public void test() throws Exception {
    final ODatabaseDocumentTx database = new ODatabaseDocumentTx("memory:defaultValues");
    try {

      database.create();

      // create example schema
      OSchema schema = database.getMetadata().getSchema();
      OClass classPerson = schema.createClass("Person");

      classPerson.createProperty("name", OType.STRING);
      classPerson.createProperty("join_date", OType.DATETIME).setDefaultValue("sysdate()");
      classPerson.createProperty("active", OType.BOOLEAN).setDefaultValue("true");

      Date dtStart = getDatabaseSysdate(database);

      ODocument[] docs = new ODocument[DOCUMENT_COUNT];
      for (int i = 0; i < DOCUMENT_COUNT; ++i) {
        ODocument doc = new ODocument("Person");
        doc.field("name", "autoGeneratedName #" + i);
        doc.save();

        docs[i] = doc;
      }

      Date dtAfter = getDatabaseSysdate(database);
      for (int i = 0; i < DOCUMENT_COUNT; ++i) {
        final ODocument doc = docs[i];

        try {
          //
          Date dt = doc.field("join_date", OType.DATETIME);

          boolean isInRange = (!dt.before(dtStart)) && (!dt.after(dtAfter));
          Assert.assertTrue(isInRange);

          //
          boolean active = doc.field("active", OType.BOOLEAN);
          Assert.assertTrue(active);
        } catch (Exception ex) {
          ex.printStackTrace();
          Assert.assertTrue(false);
        }
      }
    } finally {
      database.drop();
    }
  }

  public Date getDatabaseSysdate(ODatabaseDocumentTx database) {
    List<ODocument> dates = database.query(new OSQLSynchQuery<Date>("SELECT sysdate()"));
    return dates.get(0).field("sysdate");
  }

  @Test
  public void testDefaultValueConvertion() {
    final ODatabaseDocumentTx database = new ODatabaseDocumentTx("memory:defaultValues");
    try {
      database.create();
      OSchema schema = database.getMetadata().getSchema();
      OClass classPerson = schema.createClass("Person");
      classPerson.createProperty("users", OType.LINKSET).setDefaultValue("[#3:1]");

      ODocument doc = new ODocument("Person");
      ORecord record = database.save(doc);
      ODocument doc1 = database.load(record.getIdentity());
      Set<OIdentifiable> rids = doc1.field("users");
      assertEquals(rids.size(), 1);
      assertEquals(rids.iterator().next(), new ORecordId(3, 1));
    } finally {
      database.drop();
    }
  }
}