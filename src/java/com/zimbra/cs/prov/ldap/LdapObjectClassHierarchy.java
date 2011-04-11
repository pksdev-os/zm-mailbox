package com.zimbra.cs.account.ldap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.prov.ldap.LdapProv;

public class LdapObjectClassHierarchy {

    private static Map<String, Set<String>> sSuperOCs = new HashMap<String, Set<String>>();
    
    private static synchronized void addToSuperOCCache(Map<String, Set<String>> addition) {
        sSuperOCs.putAll(addition);
    }
    
    private static synchronized Set<String> getFromCache(String key) {
        return sSuperOCs.get(key);
    }
    
    private static boolean checkCache(String oc1, String oc2) {
        oc1 = oc1.toLowerCase();
        oc2 = oc2.toLowerCase();
        
        Set<String> supers = getFromCache(oc1);

        if (supers == null)
            return false;
        
        if (supers.contains(oc2))
            return true;
        
        for (String superOC : supers)
            if (checkCache(superOC, oc2))
                return true;
        
        return false;
    }
    
    /**
     * get the most specific OC among oc1s and oc2
     * 
     * @param oc1s
     * @param oc2
     * @return
     */
    public static String getMostSpecificOC(LdapProv prov, String[] oc1s, String oc2) {
        
        Map<String, Set<String>> ocsToLookFor = new HashMap<String, Set<String>>();
        
        for (String oc : oc1s) {
            String ocLower = oc.toLowerCase();
            // skip zimbra OCs
            if (ocLower.startsWith("zimbra"))
                continue;
            
            // publish in cache if not in yet
            if (getFromCache(ocLower) == null) {
                ocsToLookFor.put(ocLower, new HashSet<String>());
            }
        }
        
        // query LDAP schema if needed
        if (ocsToLookFor.size() > 0) {
            prov.searchOCsForSuperClasses(ocsToLookFor);  // query LDAP
            addToSuperOCCache(ocsToLookFor);         // put in cache
        }
        
        String mostSpecific = oc2;
        for (String oc : oc1s) {
            if (checkCache(oc, mostSpecific))
                mostSpecific = oc;
        }
        
        return mostSpecific;
    }
    
    public static void main(String[] args) {
        LdapProv ldapProv = (LdapProv)Provisioning.getInstance();
        System.out.println(getMostSpecificOC(ldapProv, new String[]{"zimbraAccount", "organizationalPerson", "person"}, "inetOrgPerson") + ", expecting inetOrgPerson");
        System.out.println(getMostSpecificOC(ldapProv, new String[]{"inetOrgPerson"}, "organizationalPerson")                            + ", expecting inetOrgPerson");
        System.out.println(getMostSpecificOC(ldapProv, new String[]{"organizationalPerson", "inetOrgPerson"}, "person")                  + ", expecting inetOrgPerson");
        System.out.println(getMostSpecificOC(ldapProv, new String[]{"inetOrgPerson"}, "bbb")                                             + ", expecting bbb");
        System.out.println(getMostSpecificOC(ldapProv, new String[]{"aaa"}, "inetOrgPerson")                                             + ", expecting inetOrgPerson");
        System.out.println(getMostSpecificOC(ldapProv, new String[]{"aaa"}, "inetOrgPerson")                                             + ", expecting inetOrgPerson");
        System.out.println(getMostSpecificOC(ldapProv, new String[]{"person", "inetOrgPerson"}, "organizationalPerson")                  + ", expecting inetOrgPerson");
    }

}
