package de.embl.rieslab.htsmlm.updaters;


import java.util.List;

import javax.swing.SwingWorker;

import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.emu.utils.EmuUtils;
import de.embl.rieslab.htsmlm.graph.TimeChart;

public class TimeChartUpdater {

	private TimeChart chart_;
	private UIProperty property_;
	private volatile boolean running_ = false;
	private boolean initialised_ = false;
	private UIupdater task_;
	private int idletime_;
	
	public TimeChartUpdater(TimeChart chart, UIProperty prop, int idletime){
		
		if(chart == null || prop == null) {
			throw new NullPointerException();
		}
		
		chart_ = chart;
		property_ = prop;
		idletime_ = idletime;
	}
	
	public boolean isInitialised(){
		return initialised_;
	}
	
	public boolean isRunning(){
		return running_;
	}
	
	public void startUpdater(){
		// performs sanity check
		if(!initialised_ && property_ != null) {
			if(property_.isAssigned() && EmuUtils.isNumeric(property_.getPropertyValue())) {
				initialised_ = true;
			}
		}
		
		
		if(!running_ && initialised_){
			running_ = true;
			task_ = new UIupdater( );
			task_.execute();
		}
	}
	
	public void stopUpdater(){
		running_ = false;
	}

	public void changeIdleTime(int newtime){
		idletime_ = newtime;
	}

	public void changeChart(TimeChart newchart){
		chart_ = newchart;
	}
	
	private class UIupdater extends SwingWorker<Integer, Double> {

		@Override
		protected Integer doInBackground() throws Exception {
			Double value;

			while(running_){	
				value = Double.parseDouble(property_.getPropertyValue());
				
				// round
				value = (Math.floor(value * 100) / 100);

				publish(value);

				Thread.sleep(idletime_);
			}
			return 1;
		}

		@Override
		protected void process(List<Double> chunks) {
			for(Double result : chunks){
				chart_.addPoint(result);
			}
		}
	}
}
