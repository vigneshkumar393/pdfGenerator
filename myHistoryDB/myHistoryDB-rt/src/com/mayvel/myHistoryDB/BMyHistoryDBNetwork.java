/*
 * Copyright 2025 shloklabs. All Rights Reserved.
 */

package com.mayvel.myHistoryDB;

import com.tridium.ndriver.BNNetwork;

import javax.baja.nre.annotations.NiagaraAction;
import javax.baja.nre.annotations.NiagaraProperty;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.*;


/**
 * BNetcoolNetwork models a network of devices
 *
 * @author shloklabs on 24 Mar 2025
 */
@NiagaraType
@NiagaraProperty(name = "url", type = "String",  defaultValue = "")
@NiagaraProperty(name = "port",type = "String",defaultValue = "")
@NiagaraProperty(name = "licensed", flags = Flags.READONLY, type = "BBoolean",  defaultValue = "BBoolean.FALSE")
@NiagaraAction(name = "Sync",flags = Flags.READONLY)

public class BMyHistoryDBNetwork
  extends BNNetwork
{
//region /*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
//@formatter:off
/*@ $com.mayvel.myAlarmDB.BMyAlarmDBNetwork(2332153790)1.0$ @*/
/* Generated Wed May 07 17:52:12 IST 2025 by Slot-o-Matic (c) Tridium, Inc. 2012-2025 */

  //region Property "url"

  /**
   * Slot for the {@code url} property.
   * @see #getUrl
   * @see #setUrl
   */
  public static final Property url = newProperty(0, "", null);

  /**
   * Get the {@code url} property.
   * @see #url
   */
  public String getUrl() { return getString(url); }

  /**
   * Set the {@code url} property.
   * @see #url
   */
  public void setUrl(String v) { setString(url, v, null); }

  //endregion Property "url"

  //region Property "port"

  /**
   * Slot for the {@code port} property.
   * @see #getPort
   * @see #setPort
   */
  public static final Property port = newProperty(0, "", null);

  /**
   * Get the {@code port} property.
   * @see #port
   */
  public String getPort() { return getString(port); }

  /**
   * Set the {@code port} property.
   * @see #port
   */
  public void setPort(String v) { setString(port, v, null); }

  //endregion Property "port"

  //region Property "licensed"

  /**
   * Slot for the {@code licensed} property.
   * @see #getLicensed
   * @see #setLicensed
   */
  public static final Property licensed = newProperty(Flags.READONLY, BBoolean.FALSE.as(BBoolean.class).getBoolean(), null);

  /**
   * Get the {@code licensed} property.
   * @see #licensed
   */
  public boolean getLicensed() { return getBoolean(licensed); }

  /**
   * Set the {@code licensed} property.
   * @see #licensed
   */
  public void setLicensed(boolean v) { setBoolean(licensed, v, null); }

  //endregion Property "licensed"

  //region Action "Sync"

  /**
   * Slot for the {@code Sync} action.
   * @see #Sync()
   */
  public static final Action Sync = newAction(Flags.READONLY, null);

  /**
   * Invoke the {@code Sync} action.
   * @see #Sync
   */
  public void Sync() { invoke(Sync, null, null); }

  //endregion Action "Sync"

  //region Type

  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMyHistoryDBNetwork.class);

  //endregion Type

//@formatter:on
//endregion /*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/
  /**
   * Specify name for network resources.
   */
  public String getNetworkName()
  {
    return "NetcoolNetwork";
  }

  /**
   * return device folder type
   */
  @Override
  public Type getDeviceFolderType()
  {
    return BMyHistoryDBDeviceFolder.TYPE;
  }

  /**
   * return device type
   */
  @Override
  public Type getDeviceType()
  {
    return BMyHistoryDBDevice.TYPE;
  }

  /* TODO - Add license check if needed
  @Override
  public final Feature getLicenseFeature()
  {
    return Sys.getLicenseManager().getFeature("?? vendor", "?? feature");
  }
  */

  @Override
  public void changed(Property p, Context cx)
  {
    super.changed(p, cx);
    if (!isRunning())
    {
      return;
    }

    if (p == status)
    {
      // Give any comms opportunity to respond to status changes
    }
  }
////////////////////////////////////////////////////////////////
//Utilities
////////////////////////////////////////////////////////////////

  public void started()
          throws Exception
  {
    super.started();
  }

  public void doSync () throws InterruptedException {
    System.out.println("Hello world");
  }

  public void stopped()
          throws Exception {
    super.stopped();
  }
}
