/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.nhn.pinpoint.thrift.dto;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentStat extends org.apache.thrift.TUnion<AgentStat, AgentStat._Fields> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("AgentStat");
  private static final org.apache.thrift.protocol.TField SERIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("serial", org.apache.thrift.protocol.TType.STRUCT, (short)1);
  private static final org.apache.thrift.protocol.TField PARALLEL_FIELD_DESC = new org.apache.thrift.protocol.TField("parallel", org.apache.thrift.protocol.TType.STRUCT, (short)2);
  private static final org.apache.thrift.protocol.TField CMS_FIELD_DESC = new org.apache.thrift.protocol.TField("cms", org.apache.thrift.protocol.TType.STRUCT, (short)3);
  private static final org.apache.thrift.protocol.TField G1_FIELD_DESC = new org.apache.thrift.protocol.TField("g1", org.apache.thrift.protocol.TType.STRUCT, (short)4);

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    SERIAL((short)1, "serial"),
    PARALLEL((short)2, "parallel"),
    CMS((short)3, "cms"),
    G1((short)4, "g1");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // SERIAL
          return SERIAL;
        case 2: // PARALLEL
          return PARALLEL;
        case 3: // CMS
          return CMS;
        case 4: // G1
          return G1;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.SERIAL, new org.apache.thrift.meta_data.FieldMetaData("serial", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, StatWithSerialCollector.class)));
    tmpMap.put(_Fields.PARALLEL, new org.apache.thrift.meta_data.FieldMetaData("parallel", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, StatWithParallelCollector.class)));
    tmpMap.put(_Fields.CMS, new org.apache.thrift.meta_data.FieldMetaData("cms", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, StatWithCmsCollector.class)));
    tmpMap.put(_Fields.G1, new org.apache.thrift.meta_data.FieldMetaData("g1", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, StatWithG1Collector.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(AgentStat.class, metaDataMap);
  }

  public AgentStat() {
    super();
  }

  public AgentStat(_Fields setField, Object value) {
    super(setField, value);
  }

  public AgentStat(AgentStat other) {
    super(other);
  }
  public AgentStat deepCopy() {
    return new AgentStat(this);
  }

  public static AgentStat serial(StatWithSerialCollector value) {
    AgentStat x = new AgentStat();
    x.setSerial(value);
    return x;
  }

  public static AgentStat parallel(StatWithParallelCollector value) {
    AgentStat x = new AgentStat();
    x.setParallel(value);
    return x;
  }

  public static AgentStat cms(StatWithCmsCollector value) {
    AgentStat x = new AgentStat();
    x.setCms(value);
    return x;
  }

  public static AgentStat g1(StatWithG1Collector value) {
    AgentStat x = new AgentStat();
    x.setG1(value);
    return x;
  }


  @Override
  protected void checkType(_Fields setField, Object value) throws ClassCastException {
    switch (setField) {
      case SERIAL:
        if (value instanceof StatWithSerialCollector) {
          break;
        }
        throw new ClassCastException("Was expecting value of type StatWithSerialCollector for field 'serial', but got " + value.getClass().getSimpleName());
      case PARALLEL:
        if (value instanceof StatWithParallelCollector) {
          break;
        }
        throw new ClassCastException("Was expecting value of type StatWithParallelCollector for field 'parallel', but got " + value.getClass().getSimpleName());
      case CMS:
        if (value instanceof StatWithCmsCollector) {
          break;
        }
        throw new ClassCastException("Was expecting value of type StatWithCmsCollector for field 'cms', but got " + value.getClass().getSimpleName());
      case G1:
        if (value instanceof StatWithG1Collector) {
          break;
        }
        throw new ClassCastException("Was expecting value of type StatWithG1Collector for field 'g1', but got " + value.getClass().getSimpleName());
      default:
        throw new IllegalArgumentException("Unknown field id " + setField);
    }
  }

  @Override
  protected Object standardSchemeReadValue(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TField field) throws org.apache.thrift.TException {
    _Fields setField = _Fields.findByThriftId(field.id);
    if (setField != null) {
      switch (setField) {
        case SERIAL:
          if (field.type == SERIAL_FIELD_DESC.type) {
            StatWithSerialCollector serial;
            serial = new StatWithSerialCollector();
            serial.read(iprot);
            return serial;
          } else {
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            return null;
          }
        case PARALLEL:
          if (field.type == PARALLEL_FIELD_DESC.type) {
            StatWithParallelCollector parallel;
            parallel = new StatWithParallelCollector();
            parallel.read(iprot);
            return parallel;
          } else {
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            return null;
          }
        case CMS:
          if (field.type == CMS_FIELD_DESC.type) {
            StatWithCmsCollector cms;
            cms = new StatWithCmsCollector();
            cms.read(iprot);
            return cms;
          } else {
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            return null;
          }
        case G1:
          if (field.type == G1_FIELD_DESC.type) {
            StatWithG1Collector g1;
            g1 = new StatWithG1Collector();
            g1.read(iprot);
            return g1;
          } else {
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            return null;
          }
        default:
          throw new IllegalStateException("setField wasn't null, but didn't match any of the case statements!");
      }
    } else {
      org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
      return null;
    }
  }

  @Override
  protected void standardSchemeWriteValue(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    switch (setField_) {
      case SERIAL:
        StatWithSerialCollector serial = (StatWithSerialCollector)value_;
        serial.write(oprot);
        return;
      case PARALLEL:
        StatWithParallelCollector parallel = (StatWithParallelCollector)value_;
        parallel.write(oprot);
        return;
      case CMS:
        StatWithCmsCollector cms = (StatWithCmsCollector)value_;
        cms.write(oprot);
        return;
      case G1:
        StatWithG1Collector g1 = (StatWithG1Collector)value_;
        g1.write(oprot);
        return;
      default:
        throw new IllegalStateException("Cannot write union with unknown field " + setField_);
    }
  }

  @Override
  protected Object tupleSchemeReadValue(org.apache.thrift.protocol.TProtocol iprot, short fieldID) throws org.apache.thrift.TException {
    _Fields setField = _Fields.findByThriftId(fieldID);
    if (setField != null) {
      switch (setField) {
        case SERIAL:
          StatWithSerialCollector serial;
          serial = new StatWithSerialCollector();
          serial.read(iprot);
          return serial;
        case PARALLEL:
          StatWithParallelCollector parallel;
          parallel = new StatWithParallelCollector();
          parallel.read(iprot);
          return parallel;
        case CMS:
          StatWithCmsCollector cms;
          cms = new StatWithCmsCollector();
          cms.read(iprot);
          return cms;
        case G1:
          StatWithG1Collector g1;
          g1 = new StatWithG1Collector();
          g1.read(iprot);
          return g1;
        default:
          throw new IllegalStateException("setField wasn't null, but didn't match any of the case statements!");
      }
    } else {
      throw new TProtocolException("Couldn't find a field with field id " + fieldID);
    }
  }

  @Override
  protected void tupleSchemeWriteValue(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    switch (setField_) {
      case SERIAL:
        StatWithSerialCollector serial = (StatWithSerialCollector)value_;
        serial.write(oprot);
        return;
      case PARALLEL:
        StatWithParallelCollector parallel = (StatWithParallelCollector)value_;
        parallel.write(oprot);
        return;
      case CMS:
        StatWithCmsCollector cms = (StatWithCmsCollector)value_;
        cms.write(oprot);
        return;
      case G1:
        StatWithG1Collector g1 = (StatWithG1Collector)value_;
        g1.write(oprot);
        return;
      default:
        throw new IllegalStateException("Cannot write union with unknown field " + setField_);
    }
  }

  @Override
  protected org.apache.thrift.protocol.TField getFieldDesc(_Fields setField) {
    switch (setField) {
      case SERIAL:
        return SERIAL_FIELD_DESC;
      case PARALLEL:
        return PARALLEL_FIELD_DESC;
      case CMS:
        return CMS_FIELD_DESC;
      case G1:
        return G1_FIELD_DESC;
      default:
        throw new IllegalArgumentException("Unknown field id " + setField);
    }
  }

  @Override
  protected org.apache.thrift.protocol.TStruct getStructDesc() {
    return STRUCT_DESC;
  }

  @Override
  protected _Fields enumForId(short id) {
    return _Fields.findByThriftIdOrThrow(id);
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }


  public StatWithSerialCollector getSerial() {
    if (getSetField() == _Fields.SERIAL) {
      return (StatWithSerialCollector)getFieldValue();
    } else {
      throw new RuntimeException("Cannot get field 'serial' because union is currently set to " + getFieldDesc(getSetField()).name);
    }
  }

  public void setSerial(StatWithSerialCollector value) {
    if (value == null) throw new NullPointerException();
    setField_ = _Fields.SERIAL;
    value_ = value;
  }

  public StatWithParallelCollector getParallel() {
    if (getSetField() == _Fields.PARALLEL) {
      return (StatWithParallelCollector)getFieldValue();
    } else {
      throw new RuntimeException("Cannot get field 'parallel' because union is currently set to " + getFieldDesc(getSetField()).name);
    }
  }

  public void setParallel(StatWithParallelCollector value) {
    if (value == null) throw new NullPointerException();
    setField_ = _Fields.PARALLEL;
    value_ = value;
  }

  public StatWithCmsCollector getCms() {
    if (getSetField() == _Fields.CMS) {
      return (StatWithCmsCollector)getFieldValue();
    } else {
      throw new RuntimeException("Cannot get field 'cms' because union is currently set to " + getFieldDesc(getSetField()).name);
    }
  }

  public void setCms(StatWithCmsCollector value) {
    if (value == null) throw new NullPointerException();
    setField_ = _Fields.CMS;
    value_ = value;
  }

  public StatWithG1Collector getG1() {
    if (getSetField() == _Fields.G1) {
      return (StatWithG1Collector)getFieldValue();
    } else {
      throw new RuntimeException("Cannot get field 'g1' because union is currently set to " + getFieldDesc(getSetField()).name);
    }
  }

  public void setG1(StatWithG1Collector value) {
    if (value == null) throw new NullPointerException();
    setField_ = _Fields.G1;
    value_ = value;
  }

  public boolean isSetSerial() {
    return setField_ == _Fields.SERIAL;
  }


  public boolean isSetParallel() {
    return setField_ == _Fields.PARALLEL;
  }


  public boolean isSetCms() {
    return setField_ == _Fields.CMS;
  }


  public boolean isSetG1() {
    return setField_ == _Fields.G1;
  }


  public boolean equals(Object other) {
    if (other instanceof AgentStat) {
      return equals((AgentStat)other);
    } else {
      return false;
    }
  }

  public boolean equals(AgentStat other) {
    return other != null && getSetField() == other.getSetField() && getFieldValue().equals(other.getFieldValue());
  }

  @Override
  public int compareTo(AgentStat other) {
    int lastComparison = org.apache.thrift.TBaseHelper.compareTo(getSetField(), other.getSetField());
    if (lastComparison == 0) {
      return org.apache.thrift.TBaseHelper.compareTo(getFieldValue(), other.getFieldValue());
    }
    return lastComparison;
  }


  /**
   * If you'd like this to perform more respectably, use the hashcode generator option.
   */
  @Override
  public int hashCode() {
    return 0;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }


  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }


}
