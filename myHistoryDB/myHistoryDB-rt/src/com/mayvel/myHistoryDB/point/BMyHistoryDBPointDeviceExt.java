/*
 * Copyright 2025 shloklabs. All Rights Reserved.
 */

package com.mayvel.myHistoryDB.point;

import com.mayvel.myHistoryDB.BMyHistoryDBDevice;
import com.mayvel.myHistoryDB.BMyHistoryDBNetwork;
import com.tridium.ndriver.discover.BINDiscoveryObject;
import com.tridium.ndriver.discover.BNDiscoveryPreferences;
import com.tridium.ndriver.point.BNPointDeviceExt;

import javax.baja.nre.annotations.NiagaraProperty;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.Property;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

/**
 * BNetcoolPointDeviceExt is a container for netcool proxy points.
 *
 * @author shloklabs on 24 Mar 2025
 */
@NiagaraType
@NiagaraProperty(
  name = "discoveryPreferences",
  type = "BNetcoolPointDiscoveryPreferences",
  defaultValue = "new BNetcoolPointDiscoveryPreferences()",
  override = true
)
public class BMyHistoryDBPointDeviceExt
  extends BNPointDeviceExt
{
//region /*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
//@formatter:off
/*@ $com.mayvel.myAlarmDB.point.BMyAlarmDBPointDeviceExt(3301245673)1.0$ @*/
/* Generated Wed May 07 17:52:12 IST 2025 by Slot-o-Matic (c) Tridium, Inc. 2012-2025 */

  //region Property "discoveryPreferences"

  /**
   * Slot for the {@code discoveryPreferences} property.
   * @see #getDiscoveryPreferences
   * @see #setDiscoveryPreferences
   */
  public static final Property discoveryPreferences = newProperty(0, new BMyHistoryDBPointDiscoveryPreferences(), null);

  //endregion Property "discoveryPreferences"

  //region Type

  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMyHistoryDBPointDeviceExt.class);

  //endregion Type

//@formatter:on
//endregion /*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/

////////////////////////////////////////////////////////////////
// Access
////////////////////////////////////////////////////////////////

  /**
   * Get the network cast to a BNetcoolNetwork.
   *
   * @return network as a BNetcoolNetwork.
   */
  public final BMyHistoryDBNetwork getNetcoolNetwork()
  {
    return (BMyHistoryDBNetwork) getNetwork();
  }

  /**
   * Get the device cast to a BNetcoolDevice.
   *
   * @return device as a BNetcoolDevice.
   */
  public final BMyHistoryDBDevice getNetcoolDevice()
  {
    return (BMyHistoryDBDevice) getDevice();
  }

////////////////////////////////////////////////////////////////
// PointDeviceExt
////////////////////////////////////////////////////////////////

  /**
   * @return the Device type.
   */
  public Type getDeviceType()
  {
    return BMyHistoryDBDevice.TYPE;
  }

  /**
   * @return the PointFolder type.
   */
  public Type getPointFolderType()
  {
    return BMyHistoryDBPointFolder.TYPE;
  }

  /**
   * @return the ProxyExt type.
   */
  public Type getProxyExtType()
  {
    return BMyHistoryDBProxyExt.TYPE;
  }

////////////////////////////////////////////////////////////////
//BINDiscoveryHost
////////////////////////////////////////////////////////////////

  /**
   * Call back for discoveryJob to get an array of discovery objects.
   * Override point for driver specific discovery.
   */
  public BINDiscoveryObject[] getDiscoveryObjects(BNDiscoveryPreferences prefs)
    throws Exception
  {
    //
    // TODO  get array of discovery objects
    //
//    Array<??> a = new Array<>(??.class);
//    for(??)
//     a.add(new BNetcoolPointDiscoveryLeaf(??));
//    return a.trim();
    return null;
  }
}
