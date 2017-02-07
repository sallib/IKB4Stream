package com.waves_rsp.ikb4stream.producer.datasource;

import com.waves_rsp.ikb4stream.core.datasource.IDataProducer;
import com.waves_rsp.ikb4stream.core.model.Event;

public class DataProducer implements IDataProducer {
    private final DataQueue dataQueue;

    public DataProducer() {
        dataQueue = new DataQueue();
    }

    public void push(Event event) {
        dataQueue.push(event);
    }

}