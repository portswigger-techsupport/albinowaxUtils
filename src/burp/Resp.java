package burp;

import java.util.Arrays;

class Resp implements IHttpRequestResponse {
    private IHttpRequestResponse req;
    private IResponseInfo info;
    private IResponseVariations attributes;

    public long getTimestamp() {
        return timestamp;
    }

    private long timestamp = 0;

    public long getResponseTime() {
        return responseTime;
    }

    private long responseTime = 0;

    public short getStatus() {
        return status;
    }

    private short status = 0;
    private boolean timedOut = false;
    private boolean failed = false;
    private boolean early = false;

    Resp(IHttpRequestResponse req) {
        this(req, System.currentTimeMillis());
    }

    Resp(IHttpRequestResponse req, long startTime) {
        this(req, startTime,  System.currentTimeMillis());
    }

    Resp(IHttpRequestResponse req, long startTime, long endTime) {
        this.req = req;

        byte[] fail = Utilities.helpers.stringToBytes("null");
        byte[] earlyResponse = Utilities.helpers.stringToBytes("early-response");
        // fixme will interact badly with distribute-damage
        int scanTimeout = Utilities.globalSettings.getInt("timeout") * 1000;

        early = Arrays.equals(req.getResponse(), earlyResponse);
        failed = req.getResponse() == null || req.getResponse().length == 0 || Arrays.equals(req.getResponse(), fail) || early;
        responseTime = endTime - startTime;
//        if (responseTime > 50) {
//            Utilities.out(new String(req.getRequest()));
//            throw new RuntimeException("mmm");
//        }

        if (Utilities.burpTimeout == scanTimeout) {
            if (failed && responseTime > scanTimeout) {
                this.timedOut = true;
            }
        } else {
            if (responseTime > scanTimeout) {
                this.timedOut = true;
                if (failed) {
                    Utilities.out("Timeout with response. Start time: " + startTime + " Current time: " + System.currentTimeMillis() + " Difference: " + (System.currentTimeMillis() - startTime) + " Tolerance: " + scanTimeout);
                }
            }
        }
        if (!this.failed) {
            this.status = Utilities.getCode(req.getResponse());
        }
        timestamp = System.currentTimeMillis();
    }

    IHttpRequestResponse getReq() {
        return req;
    }

    IResponseInfo getInfo() {
        if (info == null) {
            info = Utilities.helpers.analyzeResponse(req.getResponse());
        }
        return info;
    }

    IResponseVariations getAttributes() {
        if (attributes == null) {
            attributes = Utilities.helpers.analyzeResponseVariations(req.getResponse());
        }
        return attributes;
    }

    long getAttribute(String attribute) {
        switch(attribute) {
            case "time":
                return responseTime;
            case "failed":
                return failed? 1: 0;
            case "timedout":
                return timedOut? 1: 0;
            default:
                return getAttributes().getAttributeValue(attribute, 0);
        }
    }

    boolean early() {
        return early;
    }

    boolean failed() {
        return failed || timedOut;
    }

    boolean timedOut() {
        return timedOut;
    }

    @Override
    public byte[] getRequest() {
        return req.getRequest();
    }

    @Override
    public void setRequest(byte[] bytes) {
        req.setRequest(bytes);
    }

    @Override
    public byte[] getResponse() {
        return req.getResponse();
    }

    @Override
    public void setResponse(byte[] bytes) {
        req.setResponse(bytes);
    }

    @Override
    public String getComment() {
        return req.getComment();
    }

    @Override
    public void setComment(String s) {
        req.setComment(s);
    }

    @Override
    public String getHighlight() {
        return req.getHighlight();
    }

    @Override
    public void setHighlight(String s) {
        req.setHighlight(s);
    }

    @Override
    public IHttpService getHttpService() {
        return req.getHttpService();
    }

    @Override
    public void setHttpService(IHttpService iHttpService) {
        req.setHttpService(iHttpService);
    }
}
