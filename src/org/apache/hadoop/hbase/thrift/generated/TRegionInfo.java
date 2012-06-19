/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.apache.hadoop.hbase.thrift.generated;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
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

/**
 * A TRegionInfo contains information about an HTable region.
 */
public class TRegionInfo implements org.apache.thrift.TBase<TRegionInfo, TRegionInfo._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TRegionInfo");

  private static final org.apache.thrift.protocol.TField START_KEY_FIELD_DESC = new org.apache.thrift.protocol.TField("startKey", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField END_KEY_FIELD_DESC = new org.apache.thrift.protocol.TField("endKey", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.I64, (short)3);
  private static final org.apache.thrift.protocol.TField NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("name", org.apache.thrift.protocol.TType.STRING, (short)4);
  private static final org.apache.thrift.protocol.TField VERSION_FIELD_DESC = new org.apache.thrift.protocol.TField("version", org.apache.thrift.protocol.TType.BYTE, (short)5);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TRegionInfoStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TRegionInfoTupleSchemeFactory());
  }

  public ByteBuffer startKey; // required
  public ByteBuffer endKey; // required
  public long id; // required
  public ByteBuffer name; // required
  public byte version; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    START_KEY((short)1, "startKey"),
    END_KEY((short)2, "endKey"),
    ID((short)3, "id"),
    NAME((short)4, "name"),
    VERSION((short)5, "version");

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
        case 1: // START_KEY
          return START_KEY;
        case 2: // END_KEY
          return END_KEY;
        case 3: // ID
          return ID;
        case 4: // NAME
          return NAME;
        case 5: // VERSION
          return VERSION;
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

  // isset id assignments
  private static final int __ID_ISSET_ID = 0;
  private static final int __VERSION_ISSET_ID = 1;
  private BitSet __isset_bit_vector = new BitSet(2);
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.START_KEY, new org.apache.thrift.meta_data.FieldMetaData("startKey", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , "Text")));
    tmpMap.put(_Fields.END_KEY, new org.apache.thrift.meta_data.FieldMetaData("endKey", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , "Text")));
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("name", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , "Text")));
    tmpMap.put(_Fields.VERSION, new org.apache.thrift.meta_data.FieldMetaData("version", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BYTE)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TRegionInfo.class, metaDataMap);
  }

  public TRegionInfo() {
  }

  public TRegionInfo(
    ByteBuffer startKey,
    ByteBuffer endKey,
    long id,
    ByteBuffer name,
    byte version)
  {
    this();
    this.startKey = startKey;
    this.endKey = endKey;
    this.id = id;
    setIdIsSet(true);
    this.name = name;
    this.version = version;
    setVersionIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TRegionInfo(TRegionInfo other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetStartKey()) {
      this.startKey = other.startKey;
    }
    if (other.isSetEndKey()) {
      this.endKey = other.endKey;
    }
    this.id = other.id;
    if (other.isSetName()) {
      this.name = other.name;
    }
    this.version = other.version;
  }

  public TRegionInfo deepCopy() {
    return new TRegionInfo(this);
  }

  @Override
  public void clear() {
    this.startKey = null;
    this.endKey = null;
    setIdIsSet(false);
    this.id = 0;
    this.name = null;
    setVersionIsSet(false);
    this.version = 0;
  }

  public byte[] getStartKey() {
    setStartKey(org.apache.thrift.TBaseHelper.rightSize(startKey));
    return startKey == null ? null : startKey.array();
  }

  public ByteBuffer bufferForStartKey() {
    return startKey;
  }

  public TRegionInfo setStartKey(byte[] startKey) {
    setStartKey(startKey == null ? (ByteBuffer)null : ByteBuffer.wrap(startKey));
    return this;
  }

  public TRegionInfo setStartKey(ByteBuffer startKey) {
    this.startKey = startKey;
    return this;
  }

  public void unsetStartKey() {
    this.startKey = null;
  }

  /** Returns true if field startKey is set (has been assigned a value) and false otherwise */
  public boolean isSetStartKey() {
    return this.startKey != null;
  }

  public void setStartKeyIsSet(boolean value) {
    if (!value) {
      this.startKey = null;
    }
  }

  public byte[] getEndKey() {
    setEndKey(org.apache.thrift.TBaseHelper.rightSize(endKey));
    return endKey == null ? null : endKey.array();
  }

  public ByteBuffer bufferForEndKey() {
    return endKey;
  }

  public TRegionInfo setEndKey(byte[] endKey) {
    setEndKey(endKey == null ? (ByteBuffer)null : ByteBuffer.wrap(endKey));
    return this;
  }

  public TRegionInfo setEndKey(ByteBuffer endKey) {
    this.endKey = endKey;
    return this;
  }

  public void unsetEndKey() {
    this.endKey = null;
  }

  /** Returns true if field endKey is set (has been assigned a value) and false otherwise */
  public boolean isSetEndKey() {
    return this.endKey != null;
  }

  public void setEndKeyIsSet(boolean value) {
    if (!value) {
      this.endKey = null;
    }
  }

  public long getId() {
    return this.id;
  }

  public TRegionInfo setId(long id) {
    this.id = id;
    setIdIsSet(true);
    return this;
  }

  public void unsetId() {
    __isset_bit_vector.clear(__ID_ISSET_ID);
  }

  /** Returns true if field id is set (has been assigned a value) and false otherwise */
  public boolean isSetId() {
    return __isset_bit_vector.get(__ID_ISSET_ID);
  }

  public void setIdIsSet(boolean value) {
    __isset_bit_vector.set(__ID_ISSET_ID, value);
  }

  public byte[] getName() {
    setName(org.apache.thrift.TBaseHelper.rightSize(name));
    return name == null ? null : name.array();
  }

  public ByteBuffer bufferForName() {
    return name;
  }

  public TRegionInfo setName(byte[] name) {
    setName(name == null ? (ByteBuffer)null : ByteBuffer.wrap(name));
    return this;
  }

  public TRegionInfo setName(ByteBuffer name) {
    this.name = name;
    return this;
  }

  public void unsetName() {
    this.name = null;
  }

  /** Returns true if field name is set (has been assigned a value) and false otherwise */
  public boolean isSetName() {
    return this.name != null;
  }

  public void setNameIsSet(boolean value) {
    if (!value) {
      this.name = null;
    }
  }

  public byte getVersion() {
    return this.version;
  }

  public TRegionInfo setVersion(byte version) {
    this.version = version;
    setVersionIsSet(true);
    return this;
  }

  public void unsetVersion() {
    __isset_bit_vector.clear(__VERSION_ISSET_ID);
  }

  /** Returns true if field version is set (has been assigned a value) and false otherwise */
  public boolean isSetVersion() {
    return __isset_bit_vector.get(__VERSION_ISSET_ID);
  }

  public void setVersionIsSet(boolean value) {
    __isset_bit_vector.set(__VERSION_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case START_KEY:
      if (value == null) {
        unsetStartKey();
      } else {
        setStartKey((ByteBuffer)value);
      }
      break;

    case END_KEY:
      if (value == null) {
        unsetEndKey();
      } else {
        setEndKey((ByteBuffer)value);
      }
      break;

    case ID:
      if (value == null) {
        unsetId();
      } else {
        setId((Long)value);
      }
      break;

    case NAME:
      if (value == null) {
        unsetName();
      } else {
        setName((ByteBuffer)value);
      }
      break;

    case VERSION:
      if (value == null) {
        unsetVersion();
      } else {
        setVersion((Byte)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case START_KEY:
      return getStartKey();

    case END_KEY:
      return getEndKey();

    case ID:
      return Long.valueOf(getId());

    case NAME:
      return getName();

    case VERSION:
      return Byte.valueOf(getVersion());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case START_KEY:
      return isSetStartKey();
    case END_KEY:
      return isSetEndKey();
    case ID:
      return isSetId();
    case NAME:
      return isSetName();
    case VERSION:
      return isSetVersion();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TRegionInfo)
      return this.equals((TRegionInfo)that);
    return false;
  }

  public boolean equals(TRegionInfo that) {
    if (that == null)
      return false;

    boolean this_present_startKey = true && this.isSetStartKey();
    boolean that_present_startKey = true && that.isSetStartKey();
    if (this_present_startKey || that_present_startKey) {
      if (!(this_present_startKey && that_present_startKey))
        return false;
      if (!this.startKey.equals(that.startKey))
        return false;
    }

    boolean this_present_endKey = true && this.isSetEndKey();
    boolean that_present_endKey = true && that.isSetEndKey();
    if (this_present_endKey || that_present_endKey) {
      if (!(this_present_endKey && that_present_endKey))
        return false;
      if (!this.endKey.equals(that.endKey))
        return false;
    }

    boolean this_present_id = true;
    boolean that_present_id = true;
    if (this_present_id || that_present_id) {
      if (!(this_present_id && that_present_id))
        return false;
      if (this.id != that.id)
        return false;
    }

    boolean this_present_name = true && this.isSetName();
    boolean that_present_name = true && that.isSetName();
    if (this_present_name || that_present_name) {
      if (!(this_present_name && that_present_name))
        return false;
      if (!this.name.equals(that.name))
        return false;
    }

    boolean this_present_version = true;
    boolean that_present_version = true;
    if (this_present_version || that_present_version) {
      if (!(this_present_version && that_present_version))
        return false;
      if (this.version != that.version)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(TRegionInfo other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    TRegionInfo typedOther = (TRegionInfo)other;

    lastComparison = Boolean.valueOf(isSetStartKey()).compareTo(typedOther.isSetStartKey());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStartKey()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.startKey, typedOther.startKey);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetEndKey()).compareTo(typedOther.isSetEndKey());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetEndKey()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.endKey, typedOther.endKey);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetId()).compareTo(typedOther.isSetId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.id, typedOther.id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetName()).compareTo(typedOther.isSetName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.name, typedOther.name);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetVersion()).compareTo(typedOther.isSetVersion());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetVersion()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.version, typedOther.version);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("TRegionInfo(");
    boolean first = true;

    sb.append("startKey:");
    if (this.startKey == null) {
      sb.append("null");
    } else {
      sb.append(this.startKey);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("endKey:");
    if (this.endKey == null) {
      sb.append("null");
    } else {
      sb.append(this.endKey);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("id:");
    sb.append(this.id);
    first = false;
    if (!first) sb.append(", ");
    sb.append("name:");
    if (this.name == null) {
      sb.append("null");
    } else {
      sb.append(this.name);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("version:");
    sb.append(this.version);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
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
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bit_vector = new BitSet(1);
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class TRegionInfoStandardSchemeFactory implements SchemeFactory {
    public TRegionInfoStandardScheme getScheme() {
      return new TRegionInfoStandardScheme();
    }
  }

  private static class TRegionInfoStandardScheme extends StandardScheme<TRegionInfo> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TRegionInfo struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // START_KEY
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.startKey = iprot.readBinary();
              struct.setStartKeyIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // END_KEY
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.endKey = iprot.readBinary();
              struct.setEndKeyIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.id = iprot.readI64();
              struct.setIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.name = iprot.readBinary();
              struct.setNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // VERSION
            if (schemeField.type == org.apache.thrift.protocol.TType.BYTE) {
              struct.version = iprot.readByte();
              struct.setVersionIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TRegionInfo struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.startKey != null) {
        oprot.writeFieldBegin(START_KEY_FIELD_DESC);
        oprot.writeBinary(struct.startKey);
        oprot.writeFieldEnd();
      }
      if (struct.endKey != null) {
        oprot.writeFieldBegin(END_KEY_FIELD_DESC);
        oprot.writeBinary(struct.endKey);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(ID_FIELD_DESC);
      oprot.writeI64(struct.id);
      oprot.writeFieldEnd();
      if (struct.name != null) {
        oprot.writeFieldBegin(NAME_FIELD_DESC);
        oprot.writeBinary(struct.name);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(VERSION_FIELD_DESC);
      oprot.writeByte(struct.version);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TRegionInfoTupleSchemeFactory implements SchemeFactory {
    public TRegionInfoTupleScheme getScheme() {
      return new TRegionInfoTupleScheme();
    }
  }

  private static class TRegionInfoTupleScheme extends TupleScheme<TRegionInfo> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TRegionInfo struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetStartKey()) {
        optionals.set(0);
      }
      if (struct.isSetEndKey()) {
        optionals.set(1);
      }
      if (struct.isSetId()) {
        optionals.set(2);
      }
      if (struct.isSetName()) {
        optionals.set(3);
      }
      if (struct.isSetVersion()) {
        optionals.set(4);
      }
      oprot.writeBitSet(optionals, 5);
      if (struct.isSetStartKey()) {
        oprot.writeBinary(struct.startKey);
      }
      if (struct.isSetEndKey()) {
        oprot.writeBinary(struct.endKey);
      }
      if (struct.isSetId()) {
        oprot.writeI64(struct.id);
      }
      if (struct.isSetName()) {
        oprot.writeBinary(struct.name);
      }
      if (struct.isSetVersion()) {
        oprot.writeByte(struct.version);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TRegionInfo struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(5);
      if (incoming.get(0)) {
        struct.startKey = iprot.readBinary();
        struct.setStartKeyIsSet(true);
      }
      if (incoming.get(1)) {
        struct.endKey = iprot.readBinary();
        struct.setEndKeyIsSet(true);
      }
      if (incoming.get(2)) {
        struct.id = iprot.readI64();
        struct.setIdIsSet(true);
      }
      if (incoming.get(3)) {
        struct.name = iprot.readBinary();
        struct.setNameIsSet(true);
      }
      if (incoming.get(4)) {
        struct.version = iprot.readByte();
        struct.setVersionIsSet(true);
      }
    }
  }

}

