package com.example.dell.augmentedreality;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.java.model.KinveyMetaData;

/**
 * Created by dell on 08/03/2016.
 */
public class Order extends GenericJson {
    ///to make the data accessed by any one
//    @Key(KinveyMetaData.JSON_FIELD_NAME)
//    private KinveyMetaData meta;
    @Key("_acl")
    private KinveyMetaData.AccessControlList acl;

    @Key("name")
    private String name;
    @Key("requests")
    private int requests;

    @Key("available")
    private int available;

    @Key("price")
    private int price;

    @Key("owner_id")
    private int owner_id;

    @Key("_id")
    private String id;

    @Key("owner_name")
    private String owner_name;

    @Key("_kmd")
    private String kmd;

    public Order(String name, int price, int owner_id) {
        this.name = name;
        this.price = price;
        this.owner_id = owner_id;
//        meta = new KinveyMetaData();
       acl = new KinveyMetaData.AccessControlList();

    }
    public Order(){
       // acl.setGloballyReadable(true); acl.setGloballyWriteable(true);

//        meta = new KinveyMetaData();
       acl = new KinveyMetaData.AccessControlList();

    }

    /////////////

    public KinveyMetaData.AccessControlList getAcl() {
        return acl;
    }

    public void setAcl(KinveyMetaData.AccessControlList acl) {
        this.acl = acl;
    }
    public String getName() {
        return name;
    }
    public void setOwnerName(String name){
        this.owner_name=name;

    }
    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(int owner_id) {
        this.owner_id = owner_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKmd() {
        return kmd;
    }

    public void setKmd(String kmd) {
        this.kmd = kmd;
    }
    public int getRequests(){return  requests;}
    public int getAvailable(){return  available;}
    public void setRequests(int i){requests=i;}
    public String getOwner_name(){
        return owner_name;
    }
}
