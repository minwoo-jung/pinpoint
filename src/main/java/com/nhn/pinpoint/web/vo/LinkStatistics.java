package com.nhn.pinpoint.web.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.HistogramSlot;

/**
 * 
 * @author netspider
 * 
 */
public class LinkStatistics {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final int SUCCESS = 0;
	private static final int FAILED = 1;
	private static final int SLOW = Integer.MAX_VALUE - 1;
	private static final int ERROR = Integer.MAX_VALUE;

	/**
	 * <pre>
	 * key = responseTimeslot
	 * value = count
	 * </pre>
	 */
	private final SortedMap<Integer, Long> histogramSummary = new TreeMap<Integer, Long>();;

	/**
	 * <pre>
	 * key = timeslot
	 * value = { responseTimeslot, count }
	 * </pre>
	 */
	// private final Map<Long, SortedMap<Integer, Long>> timeseriesHistogram = new TreeMap<Long, SortedMap<Integer, Long>>();;

	private final SortedMap<Integer, Integer> timeseriesSlotIndex = new TreeMap<Integer, Integer>();
	
	// index = slot index, key = timestamp, value = value 
	private final List<SortedMap<Long, Long>> timeseriesValue = new ArrayList<SortedMap<Long,Long>>();
	
	/**
	 * <pre>
	 * key = timeslot
	 * value = long[0]:success, long[1]:failed
	 * </pre>
	 */
	private final Map<Long, Long[]> timeseriesFaileureHistogram = new TreeMap<Long, Long[]>();;

	private long successCount = 0;
	private long failedCount = 0;
	private final List<Integer> histogramSlotList = new ArrayList<Integer>();

	/**
	 * view에서 값이 없는 slot도 보여주기위해서..
	 * 
	 * @return
	 */
	private SortedMap<Integer, Long> makeDefaultHistogram() {
		SortedMap<Integer, Long> map = new TreeMap<Integer, Long>();
		for (int key : histogramSlotList) {
			map.put(key, 0L);
		}
		return map;
	}

	/**
	 * histogram slot을 설정하면 view에서 값이 없는 slot의 값을 0으로 보여줄 수 있다. 설정되지 않으면 key를
	 * 몰라서 보여주지 못함. 입력된 값만 보이게 됨.
	 * 
	 * @param slotList
	 */
	public void setDefaultHistogramSlotList(List<HistogramSlot> slotList) {
		if (successCount > 0 || failedCount > 0) {
			throw new IllegalStateException("Can't set slot list while containing the data.");
		}

		histogramSlotList.clear();
		histogramSummary.clear();

		// -1 is failed
		histogramSlotList.add(ERROR);
		histogramSummary.put(ERROR, 0L);

		// 0 is slow
		histogramSlotList.add(SLOW);
		histogramSummary.put(SLOW, 0L);

		for (HistogramSlot slot : slotList) {
			histogramSlotList.add(slot.getSlotTime());
			histogramSummary.put(slot.getSlotTime(), 0L);
			timeseriesSlotIndex.put(slot.getSlotTime(), timeseriesSlotIndex.size());
			timeseriesValue.add(new TreeMap<Long, Long>());
		}
	}

	public void addSample(long timestamp, int responseTimeslot, long callCount, boolean failed) {
		logger.info("Add sample. timeslot=" + timestamp + ", responseTimeslot=" + responseTimeslot + ", callCount=" + callCount + ", failed=" + failed);
		
		if (failed) {
			failedCount += callCount;
		} else {
			successCount += callCount;
		}

		// TODO 이렇게 하는게 뭔가 좋지 않은것 같음.
		if (responseTimeslot == -1) {
			responseTimeslot = ERROR;
		} else if (responseTimeslot == 0) {
			responseTimeslot = SLOW;
		}

		// add summary
		long value = histogramSummary.containsKey(responseTimeslot) ? histogramSummary.get(responseTimeslot) + callCount : callCount;
		histogramSummary.put(responseTimeslot, value);

		// add timeseries histogram
		// error:-1, slow:0가 아닌경우.
		if (responseTimeslot != ERROR && responseTimeslot != SLOW) {
			for (int i = 0; i < timeseriesValue.size(); i++) {
				SortedMap<Long, Long> map = timeseriesValue.get(i);

				// 다른 slot에도 같은 시간이 존재해야한다.
				if (i == timeseriesSlotIndex.get(responseTimeslot)) {
					long v = map.containsKey(timestamp) ? map.get(timestamp) + callCount : callCount;
					map.put(timestamp, v);
				} else {
					if (!map.containsKey(timestamp)) {
						map.put(timestamp, 0L);
					}
				}
			}
		}
		
		// add failure rate histogram
		if (timeseriesFaileureHistogram.containsKey(timestamp)) {
			Long[] array = timeseriesFaileureHistogram.get(timestamp);

			if (failed) {
				array[FAILED] += callCount;
			} else {
				array[SUCCESS] += callCount;
			}
		} else {
			Long[] array = new Long[2];
			array[SUCCESS] = 0L;
			array[FAILED] = 0L;

			if (failed) {
				array[FAILED] += callCount;
			} else {
				array[SUCCESS] += callCount;
			}
			timeseriesFaileureHistogram.put(timestamp, array);
		}
	}

	public Map<Integer, Long> getHistogramSummary() {
		return histogramSummary;
	}

	public Map<Long, Long[]> getTimeseriesFaileureHistogram() {
		return timeseriesFaileureHistogram;
	}

	public long getSuccessCount() {
		return successCount;
	}

	public long getFailedCount() {
		return failedCount;
	}

	public int getSlow() {
		return SLOW;
	}

	public int getError() {
		return ERROR;
	}

	public SortedMap<Integer, Integer> getTimeseriesSlotIndex() {
		return timeseriesSlotIndex;
	}

	public List<SortedMap<Long, Long>> getTimeseriesValue() {
		return timeseriesValue;
	}

	@Override
	public String toString() {
		return "LinkStatistics [histogramSummary=" + histogramSummary + ", timeseriesSlotIndex=" + timeseriesSlotIndex + ", timeseriesValue=" + timeseriesValue + ", timeseriesFaileureHistogram=" + timeseriesFaileureHistogram + ", successCount=" + successCount + ", failedCount=" + failedCount + ", histogramSlotList=" + histogramSlotList + "]";
	}
}