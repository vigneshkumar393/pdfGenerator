/*
 * Copyright 2025 shloklabs. All Rights Reserved.
 */

package com.mayvel.pdfGenerator.point;

import com.tridium.ndriver.discover.BNDiscoveryPreferences;

import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

/**
 * BNetcoolPointDiscoveryPreferences controls the type of discovery leafs using during
 * point discovery for netcool
 *
 * @author shloklabs on 24 Mar 2025
 */
@NiagaraType
public class BMyHistoryDBPointDiscoveryPreferences
  extends BNDiscoveryPreferences
{
//region /*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
//@formatter:off
/*@ $com.mayvel.pdfGenerator.point.BMyHistoryDBPointDiscoveryPreferences(2979906276)1.0$ @*/
/* Generated Tue Jun 24 10:31:54 IST 2025 by Slot-o-Matic (c) Tridium, Inc. 2012-2025 */

  //region Type

  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMyHistoryDBPointDiscoveryPreferences.class);

  //endregion Type

//@formatter:on
//endregion /*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/
  public BMyHistoryDBPointDiscoveryPreferences()
  {
  }

  public Type getDiscoveryLeafType()
  {
    return BMyHistoryDBPointDiscoveryLeaf.TYPE;
  }
}
