package com.cryptoguide.api.service.exchange.pipeline.source;

import com.cryptoguide.api.service.exchange.pipeline.model.NoticeItem;

import java.util.List;

public interface ExchangeNoticeSource {

    String exchange();

    List<NoticeItem> fetchLatestNotices();
}
