package com.mwg.wb.rcm.flink.module.persionalize;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import com.mwg.wb.rcm.flink.client.StateBO;

import mwg.wb.analytics.DataTrackingBO;
import mwg.wb.analytics.StateUpdateBO;
import mwg.wb.analytics.helper.Json;
 

public class TrackingProcessFunction extends ProcessWindowFunction<DataTrackingBO, StateUpdateBO, String, TimeWindow>
		implements CheckpointedFunction {

	private org.apache.flink.api.common.state.MapState<Integer, StateBO> tk_viewed_product_count_list;
	private org.apache.flink.api.common.state.MapState<Integer, StateBO> tk_viewed_product_time_list;
	private org.apache.flink.api.common.state.MapState<Integer, StateBO> tk_buy_manu_count_list;
	// danh cho tinh mua nhieu lan, list nay chua danh sach san pham va so lan mua,
	// se tang dan khi order mới có những sp này.
	private org.apache.flink.api.common.state.MapState<Integer, StateBO> tk_buy_product_count_list;// tamthoi chuyen sài
																									// củng cate

	private org.apache.flink.api.common.state.MapState<Integer, StateBO> tk_buy_productcategory_count_list;
	private org.apache.flink.api.common.state.MapState<String, StateBO> tk_buy_product_inmonth_count_list;

	private org.apache.flink.api.common.state.MapState<Integer, StateBO> tk_product_addtocart_notbuy_count_list;

	// private org.apache.flink.api.common.state.MapState<String, OrderBO>
	// history_BuyList;

	@Override
	public void snapshotState(FunctionSnapshotContext context) throws Exception {

	}

	@Override
	public void initializeState(FunctionInitializationContext context) throws Exception {

		// EXPIRE
		StateTtlConfig ttlConfig = StateTtlConfig.newBuilder(Time.days(180))// 6x30 =180 ngay=6 thang
				// .newBuilder(Time.seconds(10))
				.setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite) 
				.setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired).build();
		
	StateTtlConfig ttlConfig3week = StateTtlConfig.newBuilder(Time.days(180))// 21 ngay
				// .newBuilder(Time.seconds(10))
				.setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite) 
				.setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired).build();
	
		MapStateDescriptor<Integer, StateBO> descriptor = new MapStateDescriptor<Integer, StateBO>(
				"tk_viewed_count_list", TypeInformation.of(new TypeHint<Integer>() {
				}), TypeInformation.of(new TypeHint<StateBO>() {
				}));
		descriptor.enableTimeToLive(ttlConfig3week);
		
		tk_viewed_product_count_list = context.getKeyedStateStore().getMapState(descriptor);

		MapStateDescriptor<Integer, StateBO> descriptor0 = new MapStateDescriptor<Integer, StateBO>(
				"tk_viewed_time_list", TypeInformation.of(new TypeHint<Integer>() {
				}), TypeInformation.of(new TypeHint<StateBO>() {
				}));
		descriptor0.enableTimeToLive(ttlConfig3week);
		
		
		tk_viewed_product_time_list = context.getKeyedStateStore().getMapState(descriptor0);

		MapStateDescriptor<Integer, StateBO> descriptortk_buy_product_count_list = new MapStateDescriptor<Integer, StateBO>(
				"tk_buy_product_count_list", TypeInformation.of(new TypeHint<Integer>() {
				}), TypeInformation.of(new TypeHint<StateBO>() {
				}));
		// EXPITE data mua trong 6 tháng
		descriptortk_buy_product_count_list.enableTimeToLive(ttlConfig);
		tk_buy_product_count_list = context.getKeyedStateStore().getMapState(descriptortk_buy_product_count_list);
		// CATE MUA NHIEU
		MapStateDescriptor<Integer, StateBO> descriptortk_buy_productcategory_count_list = new MapStateDescriptor<Integer, StateBO>(
				"tk_buy_productcategory_count_list", TypeInformation.of(new TypeHint<Integer>() {
				}), TypeInformation.of(new TypeHint<StateBO>() {
				}));
		descriptortk_buy_productcategory_count_list.enableTimeToLive(ttlConfig);
		tk_buy_productcategory_count_list = context.getKeyedStateStore()
				.getMapState(descriptortk_buy_productcategory_count_list);

		MapStateDescriptor<String, StateBO> descriptortk_buy_product_inmonth_count_list = new MapStateDescriptor<String, StateBO>(
				"tk_buy_product_inmonth_count_list", TypeInformation.of(new TypeHint<String>() {
				}), TypeInformation.of(new TypeHint<StateBO>() {
				}));
		descriptortk_buy_product_inmonth_count_list.enableTimeToLive(ttlConfig);
		tk_buy_product_inmonth_count_list = context.getKeyedStateStore()
				.getMapState(descriptortk_buy_product_inmonth_count_list);

		// chưa data addtocart, mỗi user co mot danh sách này, khi có phát sinh order sẽ
		// remove data ra khỏi cái này
		MapStateDescriptor<Integer, StateBO> descriptortk_product_addtocart_notbuy_count_list = new MapStateDescriptor<Integer, StateBO>(
				"tk_product_addtocart_notbuy_count_list", TypeInformation.of(new TypeHint<Integer>() {
				}), TypeInformation.of(new TypeHint<StateBO>() {
				}));
		descriptortk_product_addtocart_notbuy_count_list.enableTimeToLive(ttlConfig);
		tk_product_addtocart_notbuy_count_list = context.getKeyedStateStore()
				.getMapState(descriptortk_product_addtocart_notbuy_count_list);

		// Expire data thống kê mua trong 6 tháng nêu KHÔNG có update

		// MANU state
		MapStateDescriptor<Integer, StateBO> descriptortk_buy_manu_count_list = new MapStateDescriptor<Integer, StateBO>(
				"tk_buy_manu_count_list", TypeInformation.of(new TypeHint<Integer>() {
				}), TypeInformation.of(new TypeHint<StateBO>() {
				}));
		// CHO EXPIRE luôn
		descriptortk_buy_manu_count_list.enableTimeToLive(ttlConfig);
		tk_buy_manu_count_list = context.getKeyedStateStore().getMapState(descriptortk_buy_manu_count_list);

		// viewedCount.get().forEach(System.out::println);
	}

	@Override
	public void process(String key,
			ProcessWindowFunction<DataTrackingBO, StateUpdateBO, String, TimeWindow>.Context context,
			Iterable<DataTrackingBO> elements, Collector<StateUpdateBO> out) throws Exception {
		
		if (key.length() < 10 || key.contains(" ")) {
			// sai data
			System.out.println("sai cmnr :" + key);
			return;
		}
		for (DataTrackingBO dataTrackingBO : elements) {

			// System.out.println("cmd :" + dataTrackingBO.cmd + " data: " +
			// dataTrackingBO.data + "");
			dataTrackingBO.cmd = dataTrackingBO.cmd + "";
			String cmd = dataTrackingBO.cmd.toUpperCase();

			if (cmd.equals("ORDER_ADD_TO_CART_PRODUCTS")) {
//				AddToCartProductBO order = Json.toObject(dataTrackingBO.data, AddToCartProductBO.class);
//				if (order != null) {
//					StateBO statebo = tk_product_addtocart_notbuy_count_list.get(order.productid);
//					if (statebo == null) {
//						statebo = new StateBO();
//						statebo.id = order.productid;
//						statebo.count = 1;
//						statebo.time = LocalDateTime.now();
//					} else {
//						statebo.count = statebo.count + 1;
//						statebo.time = LocalDateTime.now();
//					}
//
//					tk_product_addtocart_notbuy_count_list.put(order.productid, statebo);
//				}
			}
 
		StateUpdateBO obj_tk_viewed_product_count_list = BuildCollect(tk_viewed_product_count_list.values(), key,
				"tk_viewed_product_count_list");
		if (obj_tk_viewed_product_count_list != null) {
			out.collect(obj_tk_viewed_product_count_list);
		}
		}

	}

	public StateUpdateBO BuildCollect(Iterable<StateBO> vao, String username, String propertyname) {

		List<StateBO> ra = new ArrayList<StateBO>();
		if (vao != null) {
			int count = 0;
			for (StateBO bo : vao) {
				if (bo != null) {
					ra.add(bo);
					count++;
				}
				if (count > 200)
					break;// địt mẹ nhiều quá cpu leak
			}
			if (!ra.isEmpty()) {
//				Collections.sort(ra);
 				StateUpdateBO stateUpdateBO2 = new StateUpdateBO();
//				stateUpdateBO2.username = username;
//				stateUpdateBO2.statelist = ra;
//				stateUpdateBO2.propertyname = propertyname;
				return stateUpdateBO2;
			}
		}
		return null;
	}

}
