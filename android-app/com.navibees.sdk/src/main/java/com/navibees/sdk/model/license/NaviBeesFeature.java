package com.navibees.sdk.model.license;

/**
 * Created by nabilnoaman on 1/3/16.
 */
public enum NaviBeesFeature {
        Reserved_0 ,
        _2D_Maps ,
        _3D_Maps ,
        Reserved_3 ,

        Positioning ,//---------------------------
        Reserved_5 ,
        Multi_Floor_Navigation ,
        TurnByTurn_Navigation ,

        Navigation_TTS,//-----------------------
        User_Based_Navigation ,
        Reserved_10 ,
        Reserved_11 ,

        Location_Based_Notifications/*Monitoring*/ ,//--------------
        Broadcast_Notifications/*Push Notification*/,//------------
        Profile_Based_Notification ,
        Temporal_Based_Event_Activities_Notification ,

        App_Analytics ,
        Demographics_Analytics ,
        Live_Location_Based_Analytics ,
        Historical_Location_Based_Analytics ,

        Analytics_Against_KPIs,
        Reserved_21 ,
        Reserved_22 ,
        Reserved_23 ,

        Reserved_24 ,
        Reserved_25 ,
        Reserved_26 ,
        Reserved_27 ,

        Reserved_28 ,
        Reserved_29 ,
        Reserved_30 ,
        Reserved_31 ,
}
//0x00 00 31 D2 , 12754 --> currently all , adf33e0eb264bc0e42516c4df3512e56ffcf1a1b92846abbb0787135d9f5a2ba
//0x00 00 30 D2 , 12498 --> TTS disabled  , 7a09ce8ab01eb44c21f9ccb1a1cf51cf167109801abfe6404fec58c4bde2f83e
//0x00 00 30 C2 , 12482 --> Positioning disabled ,TTS disabled  , b11165e4ed9a10bc73d8f8094d37febefabebd6f993363927af289e02b42dec9
//0x00 00 20 C2 , 8386 --> Location_Based_Notifications Disabled , Positioning disabled ,TTS disabled  , 81722d37075de711db3705098428a3edd5393d76b7c28d02761634ac5052f3b6
//0x00 00 10 C2 , 4290 --> Location_Based_Notifications Enabled , Positioning disabled ,TTS disabled  , c2c0d0f48009ab14f82912b2fd43e26dad07a671f975e7b87f9bd8177d41d95b
//0x00 00 10 00 , 4096 --> Location_Based_Notifications Enabled , ba7022f43356f36d057c56cddb6b5c44d7eef905fd80833528e767838559f155

//0x00 00 00 D2 , 210 --> 2D_Map ,Positioning , Multi_Floor_Navigation , TurnByTurn_Navigation, e3e817bf56eb559a85ddc20fd41a37deef8517083822a3882f702efc915c2c73
//0x00 00 00 52 , 82 --> 2D_Map ,Positioning , Multi_Floor_Navigation , ec5a84be2ef49069ce31b2f9590aac9b8df7acff023595a5623515e485cde18d
//0x00 00 00 92 , 146 --> 2D_Map ,Positioning , TurnByTurn_Navigation , 1926b78362462f7e863ccb46bf2f488b032ae3ac93a49920adfe71d37760840e