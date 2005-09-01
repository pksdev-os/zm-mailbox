/*
 * ***** BEGIN LICENSE BLOCK *****
 * Version: ZPL 1.1
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.1 ("License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.zimbra.com/license
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 * 
 * The Original Code is: Zimbra Collaboration Suite.
 * 
 * The Initial Developer of the Original Code is Zimbra, Inc.
 * Portions created by Zimbra are Copyright (C) 2005 Zimbra, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): 
 * 
 * ***** END LICENSE BLOCK *****
 */

/*
 * Created on Mar 28, 2005
 */
package com.zimbra.cs.service.util;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.ServiceException;


/**
 * @author tim
 *
 * A mail_item ID coming off the wire can be of the format:
 * 
 *    /SERVER/MAILBOXID/MAILITEMID[-SUBID] -- different server, different mailbox    
 *    /SERVER/MAILBOXID -- mailbox on different server
 *    MAILBOXID/MAILITEMID[-SUBID]  -- local, but in another account (requires admin auth token)
 *    MAIL_ITEM_ID[-SUBID]  -- local, in this account's mail_item table
 *    
 * "SUBID" is used for composite objects (ie Appointments) which can contain sub-objects   
 * 
 * (do we also need //SERVER/SERVER-SPECIFIC-RESOURCE-DESCRIPTION?)
 * 
 *
 * IMPORTANT----->Server specifier MUST have initial "/" -- so that it is possible to differentiate
 *       SERVER/MAILITEMID from MAILBOXID/MAILITEMID 
 * 
 * Given an "id" field, parse it out into Server
 *  
 */
public class ParsedItemID { 
    
    static public ParsedItemID parse(String itemID) throws IllegalArgumentException, ServiceException {
        return new ParsedItemID(itemID);
    }
    
    static public ParsedItemID create(int mailItemId, int subId) throws ServiceException {
        // FIXME eliminate conversion through string here
        return parse(mailItemId+"-"+subId);
    }
    
    static public ParsedItemID create(int mailItemId) throws ServiceException {
        // FIXME eliminate conversion through string here
        return parse(Integer.toString(mailItemId));
    }
    
    public String getString() { return mInitialString; };
    public String toString()  { return getString(); };
    
    public boolean hasServerID()  { return mServerId != null; }
    public boolean hasMailboxID() { return mMailboxId != null; }
    public boolean hasSubId()     { return mSubId != null; }
    
    public String getServerIDString()  { return mServerId; }
    public String getMailboxIDString() { return mMailboxId; }
    public String getItemIDString()    { return mItemId; }
    public boolean isLocal()           { return mIsLocal; }

    public int getMailboxIDInt() throws ServiceException {
        if (mMailboxIdInt == -1 && mMailboxId != null)
            try {
                mMailboxIdInt = Integer.parseInt(mMailboxId.trim());
            } catch (NumberFormatException nfe) {
                throw ServiceException.INVALID_REQUEST("invalid mailbox id: " + mMailboxId, null);
            }
        return mMailboxIdInt;
    }

    public int getItemIDInt() throws ServiceException {
        if (mItemIdInt == -1 && mItemId != null)
            try {
                mItemIdInt = Integer.parseInt(mItemId.trim());
            } catch (NumberFormatException nfe) {
                throw ServiceException.INVALID_REQUEST("invalid item id: " + mItemId, null);
            }
        return mItemIdInt;
    }
    
    public int getSubIdInt() throws ServiceException {
        if (mSubIdInt == -1 && mSubId != null)
            try {
                mSubIdInt = Integer.parseInt(mSubId.trim());
            } catch (NumberFormatException nfe) {
                throw ServiceException.INVALID_REQUEST("invalid subpart id: " + mSubId, null);
            }
        return mSubIdInt;
    }


    private String mInitialString = null;

    private String mServerId = null;
    private String mMailboxId = null;
    private String mItemId = null;
    private String mSubId = null;
    
    private int mItemIdInt = -1;
    private int mMailboxIdInt = -1;
    private int mSubIdInt = -1;
    private boolean mIsLocal = true;
    
    
    /**
     * MAILITEMID or MAILITEMID-SUBID.  Sets mItemID and mSubID
     *  
     * @param itemIdPart
     * @throws ServiceException
     */
    private void parseItemIdPart(String itemIdPart) {
        int poundIdx = itemIdPart.indexOf('-');
        if (poundIdx > -1) {
            mItemId = itemIdPart.substring(0, poundIdx);
            mSubId = itemIdPart.substring(poundIdx+1);
        } else {
            mItemId = itemIdPart;
        }
    }
    
    private ParsedItemID(String itemID) throws ServiceException {
        mInitialString = itemID;
        
        String[] substrs = itemID.split("/");
        switch(substrs.length) {
        case 4:
            /* /server/mailboxid/mailitemid */
            /* /server//mailitemid */
            if (substrs[0].length() > 0)
                throw ServiceException.INVALID_REQUEST("invalid ItemID Specifier: " + itemID, null);
            if (substrs[1].length() == 0)
                throw ServiceException.INVALID_REQUEST("invalid ItemID Specifier (double initial '/'?): " + itemID, null);
            
            mServerId = substrs[1];
            String localhost = Provisioning.getInstance().getLocalServer().getAttr(Provisioning.A_zimbraServiceHostname);
            mIsLocal = mServerId.equals(localhost);
            if (substrs[2].length() > 0)
                mMailboxId = substrs[2];
            parseItemIdPart(substrs[3]);

            break;
        case 3:
            /* ERROR: /server/mailitemid  (no double-/ in middle) */
            throw ServiceException.INVALID_REQUEST("invalid ItemID Specifier (missing double-'/' before mailitemid?): " + itemID, null);
        case 2:
            /* MAILBOXID/MAILITEMID */
            mMailboxId = substrs[0];
            parseItemIdPart(substrs[1]);

            break;
        case 1:
            /* MAILITEMID */
            parseItemIdPart(substrs[0]);

            break;
        default:
            throw ServiceException.INVALID_REQUEST("invalid ItemID Specifier: " + itemID, null);
        }
    }
    
}
