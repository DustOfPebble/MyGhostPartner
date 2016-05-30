# DayToDayRace

This is a application in early devloppement phase
- It collect data from GPS and Heartbeat sensor along a way (done)
- It stores collected data into a file for futur reuse (done)
- It store GPS into a quadtree structure in order to provide fast lookup for geospatial search (done)
- It reload all previous stored files into the quad structure (done)
- It perform a lookup into the quadtree structure for the immediate neighbouring record on each GPS positon update (done)

On each lookup, it calculate some statical value about speeds, heartbeat and display them (To do) 
