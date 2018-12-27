package il.ac.bgu.cs.bp.bpjs.context.events;

import java.util.HashMap;
import java.util.Map;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class UpdateContexDBEvent extends BEvent {
	public String query;

	public Map<String, Object> parameters;

	public UpdateContexDBEvent(String query, Map<String, Object> parameters) {
		super("UpdateContexDBEvent(" + query + "," + parameters.entrySet() + ")");

		this.query = query;
		this.parameters = parameters;
	}

	public UpdateContexDBEvent(String query) {
		this(query, new HashMap<String, Object>());
	}

}