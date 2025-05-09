/*
 * Copyright 2025 shloklabs. All Rights Reserved.
 */

package com.mayvel.myHistoryDB.point;

import com.mayvel.myHistoryDB.BMyHistoryDBDevice;
import com.mayvel.myHistoryDB.BMyHistoryDBNetwork;
import com.tridium.driver.util.DrUtil;
import com.tridium.ndriver.point.BNProxyExt;

import javax.baja.driver.point.BReadWriteMode;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.status.BStatusBoolean;
import javax.baja.status.BStatusEnum;
import javax.baja.status.BStatusNumeric;
import javax.baja.status.BStatusString;
import javax.baja.sys.Context;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

/**
 * BNetcoolProxyExt
 *
 * @author shloklabs on 24 Mar 2025
 */
@NiagaraType
/*
Override ProxyExt default status to clear stale state if needed
@NiagaraProperty(
  name = "status",
  type = "BStatus",
  defaultValue = "BStatus.ok",
  flags = Flags.READONLY | Flags.TRANSIENT,
  override = true
)
*/
public class BMyHistoryDBProxyExt
  extends BNProxyExt
{
//region /*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
//@formatter:off
/*@ $com.mayvel.myAlarmDB.point.BMyAlarmDBProxyExt(2979906276)1.0$ @*/
/* Generated Wed May 07 17:52:12 IST 2025 by Slot-o-Matic (c) Tridium, Inc. 2012-2025 */

  //region Type

  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMyHistoryDBProxyExt.class);

  //endregion Type

//@formatter:on
//endregion /*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/
////////////////////////////////////////////////////////////////
// Access
////////////////////////////////////////////////////////////////

  /**
   * Get the network cast to a BNetcoolNetwork.
   */
  public final BMyHistoryDBNetwork getNetcoolNetwork()
  {
    return (BMyHistoryDBNetwork) getNetwork();
  }

  /**
   * Get the device cast to a BNetcoolDevice.
   */
  public final BMyHistoryDBDevice getBNetcoolDevice()
  {
    return (BMyHistoryDBDevice) DrUtil.getParent(this, BMyHistoryDBDevice.TYPE);
  }

  /**
   * Get the point device ext cast to a BNetcoolPointDeviceExt.
   */
  public final BMyHistoryDBPointDeviceExt getNetcoolPointDeviceExt()
  {
    return (BMyHistoryDBPointDeviceExt) getDeviceExt();
  }

////////////////////////////////////////////////////////////////
// ProxyExt
////////////////////////////////////////////////////////////////
  public void readSubscribed(Context cx)
    throws Exception
  {
    // TODO
  }

  public void readUnsubscribed(Context cx)
    throws Exception
  {
     // TODO
  }

  public boolean write(Context cx)
    throws Exception
  {
    // TODO
    return false;
  }

  /**
   * Return the device type.
   */
  public Type getDeviceExtType()
  {
    return BMyHistoryDBPointDeviceExt.TYPE;
  }

  /**
   * Return the read/write mode of this proxy.
   */
  public BReadWriteMode getMode()
  {
    // TODO
    return BReadWriteMode.readonly;
  }

  public boolean isBoolean()
  {
    return getParentPoint().getOutStatusValue() instanceof BStatusBoolean;
  }

  public boolean isNumeric()
  {
    return getParentPoint().getOutStatusValue() instanceof BStatusNumeric;
  }

  public boolean isString()
  {
    return getParentPoint().getOutStatusValue() instanceof BStatusString;
  }

  public boolean isEnum()
  {
    return getParentPoint().getOutStatusValue() instanceof BStatusEnum;
  }
}
