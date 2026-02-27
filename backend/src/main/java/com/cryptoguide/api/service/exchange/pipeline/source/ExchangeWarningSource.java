package com.cryptoguide.api.service.exchange.pipeline.source;

import com.cryptoguide.api.service.exchange.pipeline.model.WarningItem;

import java.util.List;

public interface ExchangeWarningSource {

    String exchange();

    List<WarningItem> fetchLatestWarnings();
}
