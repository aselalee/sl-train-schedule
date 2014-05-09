/**
* @copyright	Copyright (C) 2011 Asela Leelaratne
* @license		GNU/GPL Version 3
* 
* This Application is released to the public under the GNU General Public License.
* 
* GNU/GPL V3 Extract.
* 15. Disclaimer of Warranty.
* THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
* EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES
* PROVIDE THE PROGRAM AS IS WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED,
* INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
* FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE
* PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL
* NECESSARY SERVICING, REPAIR OR CORRECTION.
*/

package com.aselalee.trainschedule;

import java.util.Date;

public class Result implements Comparable<Result> {
	public String name; /* Train Name */
	public String arrivalTime_str; /* Arriving at starting station */
	public Date arrivalTime_dt;
	public String depatureTime_str; /* Departing from starting station */
	public Date depatureTime_dt;
	public String arrivalAtDestinationTime_str; /* Arrival at destination */
	public Date arrivalAtDestinationTime_dt;
	public String delayTime_str; /* Train delay information (Sometimes empty) */
	public String comment; /* Comments (Generally an empty string) */
	public String startStationName; /* Staring station of user */
	public String endStationName; /* Target destination of user */
	public String toTrStationName; /* Final destination of train*/
	public String fDescription_original; /* Train frequency */
	public String fDescription; /* Train frequency formated to fit screen */
	public String tyDescription; /* Train type */
	public String duration_str; /* Traveling time. Calculated locally. */
	
	public int compareTo(Result arg0) {
		//0 if equal, -1 if smaller, 1 if greater
		return depatureTime_dt.compareTo(arg0.depatureTime_dt);
	}
}
