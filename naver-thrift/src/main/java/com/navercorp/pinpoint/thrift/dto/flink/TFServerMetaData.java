/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.navercorp.pinpoint.thrift.dto.flink;

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
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2017-3-31")
public class TFServerMetaData implements org.apache.thrift.TBase<TFServerMetaData, TFServerMetaData._Fields>, java.io.Serializable, Cloneable, Comparable<TFServerMetaData> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TFServerMetaData");

  private static final org.apache.thrift.protocol.TField SERVER_INFO_FIELD_DESC = new org.apache.thrift.protocol.TField("serverInfo", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField VM_ARGS_FIELD_DESC = new org.apache.thrift.protocol.TField("vmArgs", org.apache.thrift.protocol.TType.LIST, (short)2);
  private static final org.apache.thrift.protocol.TField SERVICE_INFOS_FIELD_DESC = new org.apache.thrift.protocol.TField("serviceInfos", org.apache.thrift.protocol.TType.LIST, (short)10);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TFServerMetaDataStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TFServerMetaDataTupleSchemeFactory());
  }

  private String serverInfo; // optional
  private List<String> vmArgs; // optional
  private List<TFServiceInfo> serviceInfos; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    SERVER_INFO((short)1, "serverInfo"),
    VM_ARGS((short)2, "vmArgs"),
    SERVICE_INFOS((short)10, "serviceInfos");

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
        case 1: // SERVER_INFO
          return SERVER_INFO;
        case 2: // VM_ARGS
          return VM_ARGS;
        case 10: // SERVICE_INFOS
          return SERVICE_INFOS;
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
  private static final _Fields optionals[] = {_Fields.SERVER_INFO,_Fields.VM_ARGS,_Fields.SERVICE_INFOS};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.SERVER_INFO, new org.apache.thrift.meta_data.FieldMetaData("serverInfo", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.VM_ARGS, new org.apache.thrift.meta_data.FieldMetaData("vmArgs", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.SERVICE_INFOS, new org.apache.thrift.meta_data.FieldMetaData("serviceInfos", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TFServiceInfo.class))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TFServerMetaData.class, metaDataMap);
  }

  public TFServerMetaData() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TFServerMetaData(TFServerMetaData other) {
    if (other.isSetServerInfo()) {
      this.serverInfo = other.serverInfo;
    }
    if (other.isSetVmArgs()) {
      List<String> __this__vmArgs = new ArrayList<String>(other.vmArgs);
      this.vmArgs = __this__vmArgs;
    }
    if (other.isSetServiceInfos()) {
      List<TFServiceInfo> __this__serviceInfos = new ArrayList<TFServiceInfo>(other.serviceInfos.size());
      for (TFServiceInfo other_element : other.serviceInfos) {
        __this__serviceInfos.add(new TFServiceInfo(other_element));
      }
      this.serviceInfos = __this__serviceInfos;
    }
  }

  public TFServerMetaData deepCopy() {
    return new TFServerMetaData(this);
  }

  @Override
  public void clear() {
    this.serverInfo = null;
    this.vmArgs = null;
    this.serviceInfos = null;
  }

  public String getServerInfo() {
    return this.serverInfo;
  }

  public void setServerInfo(String serverInfo) {
    this.serverInfo = serverInfo;
  }

  public void unsetServerInfo() {
    this.serverInfo = null;
  }

  /** Returns true if field serverInfo is set (has been assigned a value) and false otherwise */
  public boolean isSetServerInfo() {
    return this.serverInfo != null;
  }

  public void setServerInfoIsSet(boolean value) {
    if (!value) {
      this.serverInfo = null;
    }
  }

  public int getVmArgsSize() {
    return (this.vmArgs == null) ? 0 : this.vmArgs.size();
  }

  public java.util.Iterator<String> getVmArgsIterator() {
    return (this.vmArgs == null) ? null : this.vmArgs.iterator();
  }

  public void addToVmArgs(String elem) {
    if (this.vmArgs == null) {
      this.vmArgs = new ArrayList<String>();
    }
    this.vmArgs.add(elem);
  }

  public List<String> getVmArgs() {
    return this.vmArgs;
  }

  public void setVmArgs(List<String> vmArgs) {
    this.vmArgs = vmArgs;
  }

  public void unsetVmArgs() {
    this.vmArgs = null;
  }

  /** Returns true if field vmArgs is set (has been assigned a value) and false otherwise */
  public boolean isSetVmArgs() {
    return this.vmArgs != null;
  }

  public void setVmArgsIsSet(boolean value) {
    if (!value) {
      this.vmArgs = null;
    }
  }

  public int getServiceInfosSize() {
    return (this.serviceInfos == null) ? 0 : this.serviceInfos.size();
  }

  public java.util.Iterator<TFServiceInfo> getServiceInfosIterator() {
    return (this.serviceInfos == null) ? null : this.serviceInfos.iterator();
  }

  public void addToServiceInfos(TFServiceInfo elem) {
    if (this.serviceInfos == null) {
      this.serviceInfos = new ArrayList<TFServiceInfo>();
    }
    this.serviceInfos.add(elem);
  }

  public List<TFServiceInfo> getServiceInfos() {
    return this.serviceInfos;
  }

  public void setServiceInfos(List<TFServiceInfo> serviceInfos) {
    this.serviceInfos = serviceInfos;
  }

  public void unsetServiceInfos() {
    this.serviceInfos = null;
  }

  /** Returns true if field serviceInfos is set (has been assigned a value) and false otherwise */
  public boolean isSetServiceInfos() {
    return this.serviceInfos != null;
  }

  public void setServiceInfosIsSet(boolean value) {
    if (!value) {
      this.serviceInfos = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case SERVER_INFO:
      if (value == null) {
        unsetServerInfo();
      } else {
        setServerInfo((String)value);
      }
      break;

    case VM_ARGS:
      if (value == null) {
        unsetVmArgs();
      } else {
        setVmArgs((List<String>)value);
      }
      break;

    case SERVICE_INFOS:
      if (value == null) {
        unsetServiceInfos();
      } else {
        setServiceInfos((List<TFServiceInfo>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case SERVER_INFO:
      return getServerInfo();

    case VM_ARGS:
      return getVmArgs();

    case SERVICE_INFOS:
      return getServiceInfos();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case SERVER_INFO:
      return isSetServerInfo();
    case VM_ARGS:
      return isSetVmArgs();
    case SERVICE_INFOS:
      return isSetServiceInfos();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TFServerMetaData)
      return this.equals((TFServerMetaData)that);
    return false;
  }

  public boolean equals(TFServerMetaData that) {
    if (that == null)
      return false;

    boolean this_present_serverInfo = true && this.isSetServerInfo();
    boolean that_present_serverInfo = true && that.isSetServerInfo();
    if (this_present_serverInfo || that_present_serverInfo) {
      if (!(this_present_serverInfo && that_present_serverInfo))
        return false;
      if (!this.serverInfo.equals(that.serverInfo))
        return false;
    }

    boolean this_present_vmArgs = true && this.isSetVmArgs();
    boolean that_present_vmArgs = true && that.isSetVmArgs();
    if (this_present_vmArgs || that_present_vmArgs) {
      if (!(this_present_vmArgs && that_present_vmArgs))
        return false;
      if (!this.vmArgs.equals(that.vmArgs))
        return false;
    }

    boolean this_present_serviceInfos = true && this.isSetServiceInfos();
    boolean that_present_serviceInfos = true && that.isSetServiceInfos();
    if (this_present_serviceInfos || that_present_serviceInfos) {
      if (!(this_present_serviceInfos && that_present_serviceInfos))
        return false;
      if (!this.serviceInfos.equals(that.serviceInfos))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_serverInfo = true && (isSetServerInfo());
    list.add(present_serverInfo);
    if (present_serverInfo)
      list.add(serverInfo);

    boolean present_vmArgs = true && (isSetVmArgs());
    list.add(present_vmArgs);
    if (present_vmArgs)
      list.add(vmArgs);

    boolean present_serviceInfos = true && (isSetServiceInfos());
    list.add(present_serviceInfos);
    if (present_serviceInfos)
      list.add(serviceInfos);

    return list.hashCode();
  }

  @Override
  public int compareTo(TFServerMetaData other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetServerInfo()).compareTo(other.isSetServerInfo());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetServerInfo()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.serverInfo, other.serverInfo);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetVmArgs()).compareTo(other.isSetVmArgs());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetVmArgs()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.vmArgs, other.vmArgs);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetServiceInfos()).compareTo(other.isSetServiceInfos());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetServiceInfos()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.serviceInfos, other.serviceInfos);
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
    StringBuilder sb = new StringBuilder("TFServerMetaData(");
    boolean first = true;

    if (isSetServerInfo()) {
      sb.append("serverInfo:");
      if (this.serverInfo == null) {
        sb.append("null");
      } else {
        sb.append(this.serverInfo);
      }
      first = false;
    }
    if (isSetVmArgs()) {
      if (!first) sb.append(", ");
      sb.append("vmArgs:");
      if (this.vmArgs == null) {
        sb.append("null");
      } else {
        sb.append(this.vmArgs);
      }
      first = false;
    }
    if (isSetServiceInfos()) {
      if (!first) sb.append(", ");
      sb.append("serviceInfos:");
      if (this.serviceInfos == null) {
        sb.append("null");
      } else {
        sb.append(this.serviceInfos);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
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

  private static class TFServerMetaDataStandardSchemeFactory implements SchemeFactory {
    public TFServerMetaDataStandardScheme getScheme() {
      return new TFServerMetaDataStandardScheme();
    }
  }

  private static class TFServerMetaDataStandardScheme extends StandardScheme<TFServerMetaData> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TFServerMetaData struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // SERVER_INFO
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.serverInfo = iprot.readString();
              struct.setServerInfoIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // VM_ARGS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list8 = iprot.readListBegin();
                struct.vmArgs = new ArrayList<String>(_list8.size);
                String _elem9;
                for (int _i10 = 0; _i10 < _list8.size; ++_i10)
                {
                  _elem9 = iprot.readString();
                  struct.vmArgs.add(_elem9);
                }
                iprot.readListEnd();
              }
              struct.setVmArgsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 10: // SERVICE_INFOS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list11 = iprot.readListBegin();
                struct.serviceInfos = new ArrayList<TFServiceInfo>(_list11.size);
                TFServiceInfo _elem12;
                for (int _i13 = 0; _i13 < _list11.size; ++_i13)
                {
                  _elem12 = new TFServiceInfo();
                  _elem12.read(iprot);
                  struct.serviceInfos.add(_elem12);
                }
                iprot.readListEnd();
              }
              struct.setServiceInfosIsSet(true);
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
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TFServerMetaData struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.serverInfo != null) {
        if (struct.isSetServerInfo()) {
          oprot.writeFieldBegin(SERVER_INFO_FIELD_DESC);
          oprot.writeString(struct.serverInfo);
          oprot.writeFieldEnd();
        }
      }
      if (struct.vmArgs != null) {
        if (struct.isSetVmArgs()) {
          oprot.writeFieldBegin(VM_ARGS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.vmArgs.size()));
            for (String _iter14 : struct.vmArgs)
            {
              oprot.writeString(_iter14);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.serviceInfos != null) {
        if (struct.isSetServiceInfos()) {
          oprot.writeFieldBegin(SERVICE_INFOS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.serviceInfos.size()));
            for (TFServiceInfo _iter15 : struct.serviceInfos)
            {
              _iter15.write(oprot);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TFServerMetaDataTupleSchemeFactory implements SchemeFactory {
    public TFServerMetaDataTupleScheme getScheme() {
      return new TFServerMetaDataTupleScheme();
    }
  }

  private static class TFServerMetaDataTupleScheme extends TupleScheme<TFServerMetaData> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TFServerMetaData struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetServerInfo()) {
        optionals.set(0);
      }
      if (struct.isSetVmArgs()) {
        optionals.set(1);
      }
      if (struct.isSetServiceInfos()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetServerInfo()) {
        oprot.writeString(struct.serverInfo);
      }
      if (struct.isSetVmArgs()) {
        {
          oprot.writeI32(struct.vmArgs.size());
          for (String _iter16 : struct.vmArgs)
          {
            oprot.writeString(_iter16);
          }
        }
      }
      if (struct.isSetServiceInfos()) {
        {
          oprot.writeI32(struct.serviceInfos.size());
          for (TFServiceInfo _iter17 : struct.serviceInfos)
          {
            _iter17.write(oprot);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TFServerMetaData struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.serverInfo = iprot.readString();
        struct.setServerInfoIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list18 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.vmArgs = new ArrayList<String>(_list18.size);
          String _elem19;
          for (int _i20 = 0; _i20 < _list18.size; ++_i20)
          {
            _elem19 = iprot.readString();
            struct.vmArgs.add(_elem19);
          }
        }
        struct.setVmArgsIsSet(true);
      }
      if (incoming.get(2)) {
        {
          org.apache.thrift.protocol.TList _list21 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.serviceInfos = new ArrayList<TFServiceInfo>(_list21.size);
          TFServiceInfo _elem22;
          for (int _i23 = 0; _i23 < _list21.size; ++_i23)
          {
            _elem22 = new TFServiceInfo();
            _elem22.read(iprot);
            struct.serviceInfos.add(_elem22);
          }
        }
        struct.setServiceInfosIsSet(true);
      }
    }
  }

}

