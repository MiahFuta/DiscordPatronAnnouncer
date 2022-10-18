package com.miahfuta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;

public class RoleCompare {

    private static GatewayDiscordClient client;
    
    private static String serverID;

    private static List<Member> serverMembers;

    private static List<String> oldRoles;
    private static List<String> newRoles;

    public void init(GatewayDiscordClient gatewayDiscordClient, String id) {

        client = gatewayDiscordClient;
        serverID = id;

        updateServerMembers();
        
    }

    public void updateServerMembers() {

        Snowflake server = Snowflake.of(serverID);

        serverMembers = client.getGuildMembers(server).collectList().block();

    }

    public void setOldRoles(Long memberID) {

        oldRoles = new ArrayList<String>();

        for (Member member : serverMembers) {

            Long id = member.getId().asLong();

            if (id.equals(memberID)) {

                List<Role> roles = member.getRoles().collectList().block();
                for (Role role : roles) oldRoles.add(role.getId().asString());

                break;

            }

        }

    }

    public void setNewRoles(List<Role> roles) {

        newRoles = new ArrayList<String>();

        for (Role role : roles) newRoles.add(role.getId().asString());

        cleanNewRoles();

    }

    public boolean gotRoleID(String roleID) {

        return newRoles.contains(roleID);

    }

    public boolean hadRoleID(String roleID) {

        Collection<String> oldCollection = oldRoles;
        Collection<String> newCollection = newRoles;
        
        oldCollection.removeAll(newCollection);

        List<String> removedRoles = new ArrayList<String>();
        for (String role : oldCollection) removedRoles.add(role);

        return removedRoles.contains(roleID);

    }

    private void cleanNewRoles() {
        
        Collection<String> oldCollection = oldRoles;
        Collection<String> newCollection = newRoles;
        
        newCollection.removeAll(oldCollection);

        List<String> addedRoles = new ArrayList<String>();
        for (String role : newCollection) addedRoles.add(role);

        newRoles = addedRoles;

    }
    
}