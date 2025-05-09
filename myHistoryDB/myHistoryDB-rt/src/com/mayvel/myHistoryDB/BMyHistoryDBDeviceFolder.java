/*
 * Copyright 2025 shloklabs. All Rights Reserved.
 */

package com.mayvel.myHistoryDB;

import com.tridium.ndriver.BNDeviceFolder;

import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.BComponent;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

/**
 * BNetcoolDeviceFolder is a folder for BNetcoolDevice.
 *
 * @author shloklabs on 24 Mar 2025
 */
@NiagaraType
public class BMyHistoryDBDeviceFolder
  extends BNDeviceFolder
{
//region /*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
//@formatter:off
/*@ $com.mayvel.myAlarmDB.BMyAlarmDBDeviceFolder(2979906276)1.0$ @*/
/* Generated Wed May 07 17:52:12 IST 2025 by Slot-o-Matic (c) Tridium, Inc. 2012-2025 */

  //region Type

  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMyHistoryDBDeviceFolder.class);

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
   * @return true if parent is BNetcoolNetwork or BNetcoolDeviceFolder.
   */
  public boolean isParentLegal(BComponent parent)
  {
    return parent instanceof BMyHistoryDBNetwork ||
           parent instanceof BMyHistoryDBDeviceFolder;
  }
}
