package com.example.saveetha_ec.model;

import java.math.BigDecimal;

public class GoldPriceApiResponse {

    private Long timestamp;
    private String metal;
    private String currency;
    private String exchange;
    private String symbol;
    private BigDecimal prev_close_price;
    private BigDecimal open_price;
    private BigDecimal low_price;
    private BigDecimal high_price;
    private Long open_time;
    private BigDecimal price;
    private BigDecimal ch;
    private BigDecimal chp;
    private BigDecimal ask;
    private BigDecimal bid;
    private BigDecimal price_gram_24k;
    private BigDecimal price_gram_22k;
    private BigDecimal price_gram_21k;
    private BigDecimal price_gram_20k;
    private BigDecimal price_gram_18k;
    private BigDecimal price_gram_16k;
    private BigDecimal price_gram_14k;
    private BigDecimal price_gram_10k;

    // Getters and Setters
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public String getMetal() { return metal; }
    public void setMetal(String metal) { this.metal = metal; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getPrev_close_price() { return prev_close_price; }
    public void setPrev_close_price(BigDecimal prev_close_price) { this.prev_close_price = prev_close_price; }

    public BigDecimal getOpen_price() { return open_price; }
    public void setOpen_price(BigDecimal open_price) { this.open_price = open_price; }

    public BigDecimal getLow_price() { return low_price; }
    public void setLow_price(BigDecimal low_price) { this.low_price = low_price; }

    public BigDecimal getHigh_price() { return high_price; }
    public void setHigh_price(BigDecimal high_price) { this.high_price = high_price; }

    public Long getOpen_time() { return open_time; }
    public void setOpen_time(Long open_time) { this.open_time = open_time; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getCh() { return ch; }
    public void setCh(BigDecimal ch) { this.ch = ch; }

    public BigDecimal getChp() { return chp; }
    public void setChp(BigDecimal chp) { this.chp = chp; }

    public BigDecimal getAsk() { return ask; }
    public void setAsk(BigDecimal ask) { this.ask = ask; }

    public BigDecimal getBid() { return bid; }
    public void setBid(BigDecimal bid) { this.bid = bid; }

    public BigDecimal getPrice_gram_24k() { return price_gram_24k; }
    public void setPrice_gram_24k(BigDecimal price_gram_24k) { this.price_gram_24k = price_gram_24k; }

    public BigDecimal getPrice_gram_22k() { return price_gram_22k; }
    public void setPrice_gram_22k(BigDecimal price_gram_22k) { this.price_gram_22k = price_gram_22k; }

    public BigDecimal getPrice_gram_21k() { return price_gram_21k; }
    public void setPrice_gram_21k(BigDecimal price_gram_21k) { this.price_gram_21k = price_gram_21k; }

    public BigDecimal getPrice_gram_20k() { return price_gram_20k; }
    public void setPrice_gram_20k(BigDecimal price_gram_20k) { this.price_gram_20k = price_gram_20k; }

    public BigDecimal getPrice_gram_18k() { return price_gram_18k; }
    public void setPrice_gram_18k(BigDecimal price_gram_18k) { this.price_gram_18k = price_gram_18k; }

    public BigDecimal getPrice_gram_16k() { return price_gram_16k; }
    public void setPrice_gram_16k(BigDecimal price_gram_16k) { this.price_gram_16k = price_gram_16k; }

    public BigDecimal getPrice_gram_14k() { return price_gram_14k; }
    public void setPrice_gram_14k(BigDecimal price_gram_14k) { this.price_gram_14k = price_gram_14k; }

    public BigDecimal getPrice_gram_10k() { return price_gram_10k; }
    public void setPrice_gram_10k(BigDecimal price_gram_10k) { this.price_gram_10k = price_gram_10k; }
}
