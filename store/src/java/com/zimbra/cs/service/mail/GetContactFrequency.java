package com.zimbra.cs.service.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.Pair;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.event.EventStore;
import com.zimbra.cs.event.analytics.contact.ContactAnalytics;
import com.zimbra.cs.event.analytics.contact.ContactFrequencyGraphDataPoint;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.GetContactFrequencyRequest;
import com.zimbra.soap.mail.message.GetContactFrequencyResponse;
import com.zimbra.soap.mail.type.ContactFrequencyData;
import com.zimbra.soap.mail.type.ContactFrequencyDataPoint;
import org.apache.commons.lang.StringUtils;

public class GetContactFrequency extends MailDocumentHandler {

    private static final Map<String, ContactAnalytics.ContactFrequencyGraphTimeRange> timeRangeMap = new HashMap<>();
    static {
        timeRangeMap.put("d", ContactAnalytics.ContactFrequencyGraphTimeRange.CURRENT_MONTH);
        timeRangeMap.put("w", ContactAnalytics.ContactFrequencyGraphTimeRange.LAST_SIX_MONTHS);
        timeRangeMap.put("m", ContactAnalytics.ContactFrequencyGraphTimeRange.CURRENT_YEAR);
    }

    @Override
    public Element handle(Element request, Map<String, Object> context)
            throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account acct = getRequestedAccount(zsc);
        GetContactFrequencyRequest req = zsc.elementToJaxb(request);
        String contactEmail = req.getEmail();
        String freqByOptions = req.getFrequencyBy();
        Integer offsetInMinutes = req.getOffsetInMinutes();
        EventStore eventStore = EventStore.getFactory().getEventStore(acct.getId());
        GetContactFrequencyResponse resp = new GetContactFrequencyResponse();
        for (Pair<String, ContactAnalytics.ContactFrequencyGraphTimeRange> timeRange: parseTimeRange(freqByOptions)) {
            String freqBy = timeRange.getFirst();
            ContactAnalytics.ContactFrequencyGraphTimeRange range = timeRange.getSecond();
            List<ContactFrequencyDataPoint> dataPoints = getGraphData(contactEmail, range, offsetInMinutes, eventStore);
            ContactFrequencyData graphData = new ContactFrequencyData(freqBy, dataPoints);
            resp.addFrequencyGraph(graphData);
        }
        return zsc.jaxbToElement(resp);
    }

    private List<ContactFrequencyDataPoint> getGraphData(String email, ContactAnalytics.ContactFrequencyGraphTimeRange timeRange, Integer offsetInMinutes, EventStore eventStore) throws ServiceException {
        List<ContactFrequencyGraphDataPoint> dataPoints;
        if(offsetInMinutes == null) {
            dataPoints = ContactAnalytics.getContactFrequencyGraph(email, timeRange, eventStore);
        } else {
            dataPoints = ContactAnalytics.getContactFrequencyGraph(email, timeRange, eventStore, offsetInMinutes);
        }
        List<ContactFrequencyDataPoint> soapDataPoints = dataPoints.stream().map(dp -> toSOAPDataPoint(dp)).collect(Collectors.toList());
        return soapDataPoints;
    }

    private ContactFrequencyDataPoint toSOAPDataPoint(ContactFrequencyGraphDataPoint dataPoint) {
        return new ContactFrequencyDataPoint(dataPoint.getLabel(), dataPoint.getValue());

    }
    private List<Pair<String, ContactAnalytics.ContactFrequencyGraphTimeRange>> parseTimeRange(String ranges) throws ServiceException {
        List<Pair<String, ContactAnalytics.ContactFrequencyGraphTimeRange>> requestedRanges = new ArrayList<>();
        for (Character token: Lists.charactersOf(StringUtils.deleteWhitespace(ranges))) {
            String timeRange = String.valueOf(token);
            ContactAnalytics.ContactFrequencyGraphTimeRange tr = timeRangeMap.get(timeRange);
            if (tr != null) {
                requestedRanges.add(new Pair<String, ContactAnalytics.ContactFrequencyGraphTimeRange>(timeRange, tr));
            } else {
                throw ServiceException.INVALID_REQUEST(token + " is not a valid time range; accepted values are {d w m}", null);
            }
        }
        return requestedRanges;
    }
}