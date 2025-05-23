/**
 * Autogenerated by Thrift Compiler (0.14.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.ksc.urltopn.thrift;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.14.2)", date = "2023-08-16")
public class UrlTopNAppResponse implements org.apache.thrift.TBase<UrlTopNAppResponse, UrlTopNAppResponse._Fields>, java.io.Serializable, Cloneable, Comparable<UrlTopNAppResponse> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("UrlTopNAppResponse");

  private static final org.apache.thrift.protocol.TField APPLICATION_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("applicationId", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField APP_STATUS_FIELD_DESC = new org.apache.thrift.protocol.TField("appStatus", org.apache.thrift.protocol.TType.I32, (short)2);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new UrlTopNAppResponseStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new UrlTopNAppResponseTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable java.lang.String applicationId; // required
  public int appStatus; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    APPLICATION_ID((short)1, "applicationId"),
    APP_STATUS((short)2, "appStatus");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // APPLICATION_ID
          return APPLICATION_ID;
        case 2: // APP_STATUS
          return APP_STATUS;
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
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __APPSTATUS_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.APPLICATION_ID, new org.apache.thrift.meta_data.FieldMetaData("applicationId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.APP_STATUS, new org.apache.thrift.meta_data.FieldMetaData("appStatus", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(UrlTopNAppResponse.class, metaDataMap);
  }

  public UrlTopNAppResponse() {
  }

  public UrlTopNAppResponse(
    java.lang.String applicationId,
    int appStatus)
  {
    this();
    this.applicationId = applicationId;
    this.appStatus = appStatus;
    setAppStatusIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public UrlTopNAppResponse(UrlTopNAppResponse other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetApplicationId()) {
      this.applicationId = other.applicationId;
    }
    this.appStatus = other.appStatus;
  }

  public UrlTopNAppResponse deepCopy() {
    return new UrlTopNAppResponse(this);
  }

  @Override
  public void clear() {
    this.applicationId = null;
    setAppStatusIsSet(false);
    this.appStatus = 0;
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getApplicationId() {
    return this.applicationId;
  }

  public UrlTopNAppResponse setApplicationId(@org.apache.thrift.annotation.Nullable java.lang.String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  public void unsetApplicationId() {
    this.applicationId = null;
  }

  /** Returns true if field applicationId is set (has been assigned a value) and false otherwise */
  public boolean isSetApplicationId() {
    return this.applicationId != null;
  }

  public void setApplicationIdIsSet(boolean value) {
    if (!value) {
      this.applicationId = null;
    }
  }

  public int getAppStatus() {
    return this.appStatus;
  }

  public UrlTopNAppResponse setAppStatus(int appStatus) {
    this.appStatus = appStatus;
    setAppStatusIsSet(true);
    return this;
  }

  public void unsetAppStatus() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __APPSTATUS_ISSET_ID);
  }

  /** Returns true if field appStatus is set (has been assigned a value) and false otherwise */
  public boolean isSetAppStatus() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __APPSTATUS_ISSET_ID);
  }

  public void setAppStatusIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __APPSTATUS_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case APPLICATION_ID:
      if (value == null) {
        unsetApplicationId();
      } else {
        setApplicationId((java.lang.String)value);
      }
      break;

    case APP_STATUS:
      if (value == null) {
        unsetAppStatus();
      } else {
        setAppStatus((java.lang.Integer)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case APPLICATION_ID:
      return getApplicationId();

    case APP_STATUS:
      return getAppStatus();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case APPLICATION_ID:
      return isSetApplicationId();
    case APP_STATUS:
      return isSetAppStatus();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that instanceof UrlTopNAppResponse)
      return this.equals((UrlTopNAppResponse)that);
    return false;
  }

  public boolean equals(UrlTopNAppResponse that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_applicationId = true && this.isSetApplicationId();
    boolean that_present_applicationId = true && that.isSetApplicationId();
    if (this_present_applicationId || that_present_applicationId) {
      if (!(this_present_applicationId && that_present_applicationId))
        return false;
      if (!this.applicationId.equals(that.applicationId))
        return false;
    }

    boolean this_present_appStatus = true;
    boolean that_present_appStatus = true;
    if (this_present_appStatus || that_present_appStatus) {
      if (!(this_present_appStatus && that_present_appStatus))
        return false;
      if (this.appStatus != that.appStatus)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetApplicationId()) ? 131071 : 524287);
    if (isSetApplicationId())
      hashCode = hashCode * 8191 + applicationId.hashCode();

    hashCode = hashCode * 8191 + appStatus;

    return hashCode;
  }

  @Override
  public int compareTo(UrlTopNAppResponse other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.compare(isSetApplicationId(), other.isSetApplicationId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetApplicationId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.applicationId, other.applicationId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.compare(isSetAppStatus(), other.isSetAppStatus());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAppStatus()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.appStatus, other.appStatus);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  @org.apache.thrift.annotation.Nullable
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("UrlTopNAppResponse(");
    boolean first = true;

    sb.append("applicationId:");
    if (this.applicationId == null) {
      sb.append("null");
    } else {
      sb.append(this.applicationId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("appStatus:");
    sb.append(this.appStatus);
    first = false;
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

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class UrlTopNAppResponseStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public UrlTopNAppResponseStandardScheme getScheme() {
      return new UrlTopNAppResponseStandardScheme();
    }
  }

  private static class UrlTopNAppResponseStandardScheme extends org.apache.thrift.scheme.StandardScheme<UrlTopNAppResponse> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, UrlTopNAppResponse struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // APPLICATION_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.applicationId = iprot.readString();
              struct.setApplicationIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // APP_STATUS
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.appStatus = iprot.readI32();
              struct.setAppStatusIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, UrlTopNAppResponse struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.applicationId != null) {
        oprot.writeFieldBegin(APPLICATION_ID_FIELD_DESC);
        oprot.writeString(struct.applicationId);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(APP_STATUS_FIELD_DESC);
      oprot.writeI32(struct.appStatus);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class UrlTopNAppResponseTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public UrlTopNAppResponseTupleScheme getScheme() {
      return new UrlTopNAppResponseTupleScheme();
    }
  }

  private static class UrlTopNAppResponseTupleScheme extends org.apache.thrift.scheme.TupleScheme<UrlTopNAppResponse> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, UrlTopNAppResponse struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetApplicationId()) {
        optionals.set(0);
      }
      if (struct.isSetAppStatus()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetApplicationId()) {
        oprot.writeString(struct.applicationId);
      }
      if (struct.isSetAppStatus()) {
        oprot.writeI32(struct.appStatus);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, UrlTopNAppResponse struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.applicationId = iprot.readString();
        struct.setApplicationIdIsSet(true);
      }
      if (incoming.get(1)) {
        struct.appStatus = iprot.readI32();
        struct.setAppStatusIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

