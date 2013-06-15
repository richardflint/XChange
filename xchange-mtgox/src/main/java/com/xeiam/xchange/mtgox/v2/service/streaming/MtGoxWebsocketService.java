/**
 * Copyright (C) 2013 Xeiam LLC http://xeiam.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xeiam.xchange.mtgox.v2.service.streaming;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.mtgox.MtGoxUtils;
import com.xeiam.xchange.service.streaming.BaseWebSocketExchangeService;
import com.xeiam.xchange.service.streaming.ExchangeEventListener;
import com.xeiam.xchange.service.streaming.StreamingExchangeService;
import com.xeiam.xchange.utils.Assert;

/**
 * <p>
 * Streaming trade service for the MtGox exchange
 * </p>
 * <p>
 * MtGox provides a Websocket implementation
 * </p>
 */
public class MtGoxWebsocketService extends BaseWebSocketExchangeService implements StreamingExchangeService {

  private final Logger logger = LoggerFactory.getLogger(MtGoxWebsocketService.class);

  private final ExchangeEventListener exchangeEventListener;

  /**
   * Ensures that exchange-specific configuration is available
   */
  private final MtGoxStreamingConfiguration configuration;

  /**
   * Constructor
   * 
   * @param exchangeSpecification The {@link ExchangeSpecification}
   */
  public MtGoxWebsocketService(ExchangeSpecification exchangeSpecification, MtGoxStreamingConfiguration configuration) {

    super(exchangeSpecification, configuration);

    Assert.notNull(configuration, "configuration cannot be null");
    Assert.notNull(configuration.getTradeableIdentifier(), "tradableIdentifier cannot be null");
    Assert.notNull(configuration.getCurrencyCode(), "currencyCode cannot be null");
    Assert.isTrue(MtGoxUtils.isValidCurrencyPair(new CurrencyPair(configuration.getTradeableIdentifier(), configuration.getCurrencyCode())), "currencyPair is not valid:"
        + configuration.getTradeableIdentifier() + " " + configuration.getCurrencyCode());

    this.configuration = configuration;

    // Create the listener for the specified eventType
    this.exchangeEventListener = new MtGoxExchangeEventListener(consumerEventQueue);

  }

  @Override
  public void connect() {

    String apiBase = null;
    if (configuration.isEncryptedChannel()) {
      apiBase = String.format("%s:%s/mtgox", exchangeSpecification.getSslUriStreaming(), exchangeSpecification.getPort());
    }
    else {
      apiBase = String.format("%s:%s/mtgox", exchangeSpecification.getPlainTextUriStreaming(), exchangeSpecification.getPort());
    }
    // URI uri = URI.create(apiBase + "?Channel=dbf1dee9-4f2e-4a08-8cb7-748919a71b21");
    // URI uri = URI.create(apiBase + "?Channel=ticker." + configuration.getTradeableIdentifier() + configuration.getCurrencyCode());
    URI uri = URI.create(apiBase + "?Currency=" + configuration.getCurrencyCode());
    // URI uri = URI.create(apiBase);
    Map<String, String> headers = new HashMap<String, String>(1);
    headers.put("Origin", String.format("%s:%s", exchangeSpecification.getHost(), exchangeSpecification.getPort()));

    logger.debug("Streaming URI='{}'", uri);

    // Use the default internal connect
    internalConnect(uri, exchangeEventListener, headers);
  }
}
