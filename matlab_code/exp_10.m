XMin= 599435.5810000002;
YMin= 2358326.3035000004;
XMax= 599518.2827000003;
YMax= 2358386.9219000004;
[I,map] = imread('map_no_routing.png','png');
yImg = linspace(YMax, YMin, size(I, 1));
xImg = linspace(XMin, XMax, size(I, 2));
image(xImg, yImg, I, 'CDataMapping', 'scaled');
set(gca,'Ydir','normal');
hold on

truth = [
%    timestamp       x          y
    42523.47072	599509.5815	2358351.03
    42523.47084	599508.7084	2358353.914
    42523.47089	599507.8617	2358356.983
    42523.47098	599507.2002	2358358.518
    42523.47103	599505.9302	2358361.005
    42523.47106	599504.2369	2358363.201
    42523.47113	599501.5117	2358362.486
    42523.4712	599498.1515	2358361.402
    42523.47132	599491.9338	2358360.476
    42523.47139	599488.2296	2358360.476
    42523.47157	599480.2622	2358359.063
    42523.47174	599475.4468	2358366.366
    42523.47181	599474.1504	2358370.044
    42523.47186	599471.2135	2358370.626
    42523.47194	599467.3505	2358370.52
    42523.47213	599467.3768	2358364.329
    42523.47227	599466.6624	2358359.936
    42523.47231	599464.8632	2358356.259
    42523.4724	599465.5929	2358352.636
    42523.47245	599464.7462	2358350.863
    42523.47251	599462.5502	2358348.35
    42523.4726	599459.9308	2358347.556
    42523.47269	599456.7764	2358344.75
    42523.47274	599452.1991	2358342.766
    42523.47282	599453.3632	2358339.353
    42523.47286	599454.1305	2358337.474
    42523.47289	599454.501	2358335.622
    42523.47295	599455.8768	2358333.347
    42523.47303	599455.4005	2358330.793
    
   ];

x= truth(:,2);
y= truth(:,3);
Y= dct(y);Y1=Y;Y1(11:end)=0;y1 = idct(Y1);
X= dct(x);X1=X;X1(11:end)=0;x1 = idct(X1);
%  plot(x1, y1);
  plot(x, y, '--b','LineWidth',2);




beacons = [
%Minor  x       y           floor
1	599468.358	2358372.647	0
2	599471.1361	2358373.771	0
3	599466.1752	2358367.09	0
4	599475.4356	2358374.499	0
5	599478.5445	2358366.429	0
6	599470.144	2358361.402	0
7	599477.0231	2358357.962	0
8	599482.7117	2358356.838	0
9	599472.8559	2358352.075	0
10	599468.4903	2358353.597	0
11	599467.3658	2358348.437	0
12	599462.1403	2358351.017	0
13	599451.5569	2358347.18	0
14	599460.0236	2358344.204	0
15	599454.1366	2358341.227	0
16	599448.0512	2358346.056	0
17	599450.7632	2358339.971	0
18	599452.3507	2358336.134	0
19	599458.5023	2358330.512	0
20	599464.6538	2358356.044	0
21	599466.1752	2358362.857	0
22	599484.6299	2358366.76	0
23	599490.8476	2358363.188	0
24	599496.7346	2358359.748	0
25	599489.1278	2358359.087	0
26	599501.4971	2358365.437	0
27	599507.2518	2358362.328	0
28	599504.7383	2358357.764	0
30	599507.5164	2358351.083	0
31	599508.6409	2358358.624	1
32	599503.3492	2358363.717	1
33	599503.9445	2358368.215	1
34	599499.6451	2358364.643	1
35	599493.0305	2358363.254	1
36	599492.5013	2358359.153	1
37	599485.0929	2358362.791	1
38	599482.1825	2358356.044	1
39	599479.4044	2358363.452	1
40	599477.5523	2358354.523	1
41	599470.2762	2358354.126	1
42	599469.2179	2358349.892	1
43	599463.9924	2358346.254	1
44	599461.9419	2358351.149	1
45	599459.0976	2358343.675	1
46	599452.0861	2358347.114	1
47	599448.3158	2358345.527	1
48	599454.2028	2358341.426	1
49	599451.9538	2358337.457	1
50	599493.7581	2358356.242	1
51	599498.3221	2358354.39	1
52	599488.7971	2358358.822	1
53	599456.7163	2358349.099	1
54	599449.7048	2358342.352	1
55	599506.3258	2358364.246	1
56	599479.6028	2358360.211	1
57	599462.8679	2358359.417	1
58	599459.3622	2358357.698	1
71	599509.3685	2358356.904	0
72	599458.7007	2358349.694	0
];


data = {
%id  timestamp  event(1:in, 0:out) triggered_by  covered_by

'TuGMs3xP3H'	42523.47068	0	27	[26 28 30 6 71]
'26o5A04f36'	42523.47073	0	6	[26 28 30 71]
'Fjk0GTTftU'	42523.47103	0	30	[26 28 71]
'zf082BTDeo'	42523.4711	1	23	[23 26 28 71]
'kDu8FD1B5T'	42523.47113	1	3	[23 26 28 3 71]
'xDo2AFLR3B'	42523.47119	1	25	[23 25 26 28 3 71]
'Xe8uHPg8Js'	42523.4712	1	22	[22 23 25 26 28 3 71]
'fXg7BFJVsz'	42523.47123	0	71	[22 23 25 26 28 3]
'r9vc8q5ThA'	42523.47125	0	27	[22 23 25 26 28 3]
'DcfVBRlVfS'	42523.47131	1	8	[22 23 25 26 28 3 8]
'mZrjYAv0Nq'	42523.47131	1	4	[22 23 25 26 28 3 4 8]
'd8d6afvUKT'	42523.47132	1	5	[22 23 25 26 28 3 4 5 8]
'GwYQIli3mf'	42523.47135	0	28	[22 23 25 26 3 4 5 8]
'5toLA77ADj'	42523.47138	1	9	[22 23 25 26 3 4 5 8 9]
'4iNYcBu2VD'	42523.47139	1	21	[21 22 23 25 26 3 4 5 8 9]
'TSza50uqQL'	42523.47142	1	11	[11 21 22 23 25 26 3 4 5 8 9]
'8GitMh4Anq'	42523.47148	1	2	[11 2 21 22 23 25 26 3 4 5 8 9]
'Eyb6iGHF6E'	42523.47148	0	26	[11 2 21 22 23 25 3 4 5 8 9]
'7iI2TgGNSX'	42523.47153	0	9	[11 2 21 22 23 25 3 4 5 8]
'DqDHbzit9y'	42523.47154	0	25	[11 2 21 22 23 3 4 5 8]
'eep1ejkNsc'	42523.47155	0	11	[2 21 22 23 3 4 5 8]
'5UgaVRt6tl'	42523.4716	0	2	[21 22 23 3 4 5 8]
'Qf9vLxWZek'	42523.47161	0	3	[21 22 23 4 5 8]
'lSUFWc1rAB'	42523.47162	1	9	[21 22 23 4 5 8 9]
'BrbfkkDQMf'	42523.47163	0	8	[21 22 23 4 5 9]
'uQVDcvx5IQ'	42523.47167	1	63	[21 22 23 4 5 63 9]
'rEr26ER873'	42523.47168	1	61	[21 22 23 4 5 61 63 9]
'bDU6kzeOzM'	42523.47169	1	3	[21 22 23 3 4 5 61 63 9]
'bILLBakOxh'	42523.4717	1	2	[2 21 22 23 3 4 5 61 63 9]
'gza4aPTss8'	42523.4717	0	21	[2 22 23 3 4 5 61 63 9]
's5reNrb49g'	42523.47171	1	15	[15 2 22 23 3 4 5 61 63 9]
'Pjjfajzy7e'	42523.47171	0	22	[15 2 23 3 4 5 61 63 9]
'0zizdbRMwx'	42523.47174	0	9	[15 2 23 3 4 5 61 63]
'1w7aAj5RMW'	42523.47177	0	23	[15 2 3 4 5 61 63]
'IycjF5xMqI'	42523.47178	1	21	[15 2 21 3 4 5 61 63]
'NgDmJZRK75'	42523.47179	0	63	[15 2 21 3 4 5 61]
'RybZyrrd3E'	42523.47179	0	61	[15 2 21 3 4 5]
'BstVWI5Sbe'	42523.47181	1	6	[15 2 21 3 4 5 6]
'eRrrYSkRE3'	42523.47183	1	9	[15 2 21 3 4 5 6 9]
'lPssLzNVnS'	42523.47183	0	15	[2 21 3 4 5 6 9]
'vd3iQ0vEJV'	42523.47188	0	5	[2 21 3 4 6 9]
'dyeRJvLxwU'	42523.47193	1	63	[2 21 3 4 6 63 9]
'kjmweJDd0e'	42523.47194	1	62	[2 21 3 4 6 62 63 9]
'G6byiu3wXJ'	42523.47196	0	9	[2 21 3 4 6 62 63]
'yoHRMMPmEW'	42523.47196	1	15	[15 2 21 3 4 6 62 63]
'Dn9VqKM9qN'	42523.47196	1	23	[15 2 21 23 3 4 6 62 63]
'SIpMSRbxl3'	42523.47199	0	6	[15 2 21 23 3 4 62 63]
'EyozXZW0Lb'	42523.47201	0	2	[15 21 23 3 4 62 63]
'huLJPrHnM5'	42523.47201	0	5	[15 21 23 3 4 62 63]
'stFiAbUEL6'	42523.47204	1	20	[15 20 21 23 3 4 62 63]
'ClB2xcd03f'	42523.47207	0	62	[15 20 21 23 3 4 63]
'wJ5gLjvNtF'	42523.47207	0	63	[15 20 21 23 3 4]
'0BX3Yod0Ve'	42523.47208	0	15	[20 21 23 3 4]
'whJBB8fPMe'	42523.47208	0	4	[20 21 23 3]
'RI5m1qVzF2'	42523.47208	0	21	[20 23 3]
'sevOLclG7C'	42523.47209	0	23	[20 3]
'inneENlcs6'	42523.47219	1	63	[20 3 63]
'8Fea8CAO9k'	42523.47222	1	10	[10 20 3 63]
'cWVA4lYUlj'	42523.47222	0	15	[10 20 3 63]
'wvFGdeZYXL'	42523.47228	0	3	[10 20 63]
'MFg3TOHL7j'	42523.47228	1	15	[10 15 20 63]
'0ugQ2Su1F2'	42523.47231	1	9	[10 15 20 63 9]
'My5McyXXfp'	42523.47231	0	63	[10 15 20 9]
'1PoQVxb60O'	42523.47233	1	11	[10 11 15 20 9]
'IGcHTtwDuw'	42523.47234	1	12	[10 11 12 15 20 9]
'2M0UDblScz'	42523.47238	1	13	[10 11 12 13 15 20 9]
'3fv7eGmSZy'	42523.4724	1	17	[10 11 12 13 15 17 20 9]
'gJUR9xdhrw'	42523.4724	1	72	[10 11 12 13 15 17 20 72 9]
'GylJUStpRk'	42523.4724	1	14	[10 11 12 13 14 15 17 20 72 9]
'Yz9WaF9g2R'	42523.47242	0	6	[10 11 12 13 14 15 17 20 72 9]
'q3uvR1ngoV'	42523.47243	1	23	[10 11 12 13 14 15 17 20 23 72 9]
'Q7TGjn2UlQ'	42523.47244	0	15	[10 11 12 13 14 17 20 23 72 9]
'acQSuGUi17'	42523.47245	0	21	[10 11 12 13 14 17 20 23 72 9]
'Xxw1ffMGeY'	42523.47252	1	15	[10 11 12 13 14 15 17 20 23 72 9]
'7bVKpiGIzB'	42523.47256	0	23	[10 11 12 13 14 15 17 20 72 9]
'NUxpEmnwca'	42523.47259	1	16	[10 11 12 13 14 15 16 17 20 72 9]
'7nlFnaBCeW'	42523.47262	1	18	[10 11 12 13 14 15 16 17 18 20 72 9]
'2WwHQXsFQ5'	42523.47278	1	19	[10 11 12 13 14 15 16 17 18 19 20 72 9]
'nnIQTw2xiG'	42523.47285	0	9	[10 11 12 13 14 15 16 17 18 19 20 72]
'D5c7HucMLM'	42523.47293	0	20	[10 11 12 13 14 15 16 17 18 19 72]
'0Gax6103nI'	42523.47293	0	11	[10 12 13 14 15 16 17 18 19 72]
'LSXII5jwbw'	42523.47297	0	9	[10 12 13 14 15 16 17 18 19 72]
'afZtDqpfFz'	42523.47302	0	12	[10 13 14 15 16 17 18 19 72]
'FAG9kwEjWh'	42523.47304	0	10	[13 14 15 16 17 18 19 72]
};


in_timestamps = cell2mat(data(find(cell2mat(data(:,3))==[1]),[2 4]));
%uniformaly sampling data
p = 1/86400;  % 1 second in a day
tstamps = cell2mat(data(:,2));
sampled = {};
for t = min(tstamps)+p:p:max(tstamps) + p
    tags = data(max(find (cell2mat(data(:,2))<t)),5);
    sampled(end+1,1:2) = [t tags];
end


trajectory = [];

 for n=1:length(sampled)
     t=cell2mat(sampled(n));
     coverage = cell2mat(sampled(n,2));
      coverage = transpose(intersect(coverage, beacons));
%       coverage = transpose(intersect (coverage,in_timestamps(:,2)))
     if size(coverage) > 0
%           % Hierarchical filtering
             X = beacons(ismember(beacons(:,1),transpose(coverage)), 1:3);
             Y = pdist(X(:,2:3));
             Z = linkage(Y);
             T = cluster(Z,'maxclust',2);
             if size(T(T==1)) >= size(T(T==2))
                 culster_id = 1;
             else
                 culster_id = 2;
             end
             original_coverage = coverage
             coverage = transpose (intersect(X(T==culster_id), coverage))
%           % end filtering
         
         
         last_in = [];
         for tag = coverage
             appeared_on = in_timestamps(max(find(in_timestamps(:,2)==tag & in_timestamps(:,1) <= t)));

             if isempty(appeared_on)
                 appeared_on = min(cell2mat(sampled(:,1)));
             end
             last_in(end+1,1) = appeared_on;
         end
    %      last_in= (last_in*1000) - min(last_in)
         if size(coverage) <=3
             last_in(:,1) = 1;
         else
                if range(last_in) == 0
                    last_in(:,1) = 1;
                else
                    last_in = last_in - min(last_in);
                    last_in = last_in/sum(last_in) * 100;
                end

         end
     
     
%          avg_x = mean (beacons(ismember(beacons(:,1),transpose(coverage)), 2));
%          avg_y = mean (beacons(ismember(beacons(:,1),transpose(coverage)), 3));
%          x(end+1) = avg_x;
%          y(end+1) = avg_y;

         wavg_x = sum (beacons(ismember(beacons(:,1),transpose(coverage)), 2) .* last_in)/sum(last_in);
          wavg_y = sum (beacons(ismember(beacons(:,1),transpose(coverage)), 3) .* last_in)/sum(last_in);
          trajectory(end+1,:) = [t, wavg_x, wavg_y];
          
     end
 end


% x=[];
% y=[];
% lookAhead = 2;
% for n=1:length(data)-lookAhead
%     coverage = cell2mat(data(n,5));
%     for l=1:lookAhead
%         coverage = intersect(coverage, cell2mat(data(n+l,5)));
%     end
%     if size(coverage)< 4
%         coverage = cell2mat(data(n,5));
%     end
%     if size(coverage) > 0
%         avg_x = mean(beacons(ismember(beacons(:,1),transpose(coverage)), 2));
%         avg_y = mean(beacons(ismember(beacons(:,1),transpose(coverage)), 3));
%         x(end+1) = avg_x;
%         y(end+1) = avg_y;
%     end
% end

x = trajectory(:,2);
y = trajectory(:,3);

Y= dct(y);Y1=Y;Y1(11:end)=0;y1 = idct(Y1);
X= dct(x);X1=X;X1(11:end)=0;x1 = idct(X1);
plot(x1, y1,'r','LineWidth',2);


        
legend('Ground Truth', 'Estimated Trajectory')

[x1 y1]