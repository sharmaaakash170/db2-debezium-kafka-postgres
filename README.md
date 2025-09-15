I’ve done a detailed round of debugging to identify why our GKE pods are intermittently failing to connect to the on-prem DB2 service (port 60004 on 172.18.75.70). Below is the summary of my findings:

GCP side validation:
I confirmed that the NAT VM (10.0.32.213) is correctly performing SNAT for all pod traffic destined for the on-prem IP.
Verified with tcpdump that packets leave GKE pods → NAT VM → On-Prem without being dropped.
Our GCP firewall rules explicitly allow egress from NAT VM to the on-prem host/port. No drops observed in GCP logs.
Public egress IP of the pods (34.93.123.1) was confirmed, and it is consistent.

Observed behavior on traffic flow:
During nc/curl tests, sometimes the TCP handshake completes successfully (SYN → SYN/ACK → ACK) and DB2 port responds as open.
Other times, packets are either not responded to or connections are immediately reset/closed after handshake.
This inconsistency was captured both from the pod side and using tcpdump on the NAT VM.
Why the issue is not on GCP side:
Routing tables are correct (confirmed custom route 172.18.75.70/32 → NAT VM).
SNAT and return-path translation work fine when connection succeeds.
No packet drops or firewall denials are seen in GCP.
Most probable cause (on-prem side):
Firewall rules: On-prem firewall is likely not consistently allowing traffic from GCP IP ranges (only partially or intermittently).
DB2 listener config: The DB2 listener on port 60004 may be closing sessions immediately or rejecting new connections under load.
Stateful inspection / session limits: On-prem firewall/IPS could be dropping sessions due to connection limits or asymmetric routing.
Conclusion:
From all tests and evidence, the issue is not on the GCP side. Networking (routes, NAT, firewall) is functioning as expected. The intermittent connectivity is originating on the on-prem side (firewall or DB2 listener).

Next Steps I suggest:
Customer should check their firewall logs for drops against source IP 34.93.123.1.
Validate that their DB2 listener is stable and not closing sessions prematurely.
Ensure all GCP pod CIDRs or the NAT VM IP range is fully whitelisted, not just partially.

GKE Pod (10.48.x.x)  
      │  
      ▼  
NAT VM (10.0.32.213 → SNAT → 34.93.123.1)  
      │  
      ▼  
On-Prem Firewall / VPN  
      │  
      ▼  
DB2 Server (172.18.75.70:60004)