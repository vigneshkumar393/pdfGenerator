/*
 * Copyright 2025 shloklabs. All Rights Reserved.
 */

package com.mayvel.myHistoryDB.point;

import com.mayvel.myHistoryDB.BMyHistoryDBDevice;
import com.mayvel.myHistoryDB.BMyHistoryDBNetwork;
import com.tridium.ndriver.point.BNPointFolder;

import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

/**
 * BNetcoolPointFolder
 *
 * @author   shloklabs on 24 Mar 2025
 */
@NiagaraType
public class BMyHistoryDBPointFolder
  extends BNPointFolder
{
//region /*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
//@formatter:off
/*@ $com.mayvel.myAlarmDB.point.BMyAlarmDBPointFolder(2979906276)1.0$ @*/
/* Generated Wed May 07 17:52:12 IST 2025 by Slot-o-Matic (c) Tridium, Inc. 2012-2025 */

  //region Type

  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMyHistoryDBPointFolder.class);

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
}
