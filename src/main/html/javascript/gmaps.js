var zoom = 15;
var refreshRate = 1;
var map;
var icons = {
    tower: {
      icon: 'https://maps.google.com/mapfiles/dir_0.png'
    },
    antenna: {
      icon: 'https://maps.google.com/mapfiles/kml/pal4/icon49.png'
    },
    mapCenter: {
      icon: 'https://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png'
    }
  };
var positions = [];
var towers = [];
var mapCenter = [];
var deviceId = 0;
var nbPoints = document.getElementById('nbPoints');
var infowindow;

function addInfo(marker, header, info){
	var markerInfo = '<table>';

	for(var n = 0; n < info.length; n += 1){
		name = header[n].replace(/\[.*\]/, '');
		unit = header[n].replace(/[^\[]*\[?/, ' ').replace(']', '');
		value = info[n].replace('Z[Etc/UTC]', ' UTC');
		markerInfo += '<tr><td>' + name + ':</td><td>' + value + unit + '</td></tr>';
	}
	markerInfo += '</table>';
	marker.addListener('click', function() {
		infowindow.close();
		infowindow.setContent(markerInfo);
		infowindow.open(map, marker);
    });
}

function addMarker(markers, position, type, header, info){
	if(markers != null){
		for(var n = 0; n < markers.length; n += 1){
			if(markers[n].position.lat() == position[1] && markers[n].position.lng() == position[0]){
				return markers[n];
			}
		}
	}

	const marker = new google.maps.Marker({
		position: new google.maps.LatLng(position[1], position[0]),
		icon: icons[type].icon,
		map: map
	});

	if(markers != null){
		markers.push(marker);
	}

	return marker;
}

function addMarkers(positions, type){
	for(var i = 0; i < positions.length; i+=2){
		var position = [positions[i], positions[i + 1]];
		addMarker(null, position, type);
	}
}

function showMarkers(markers){
	for(let i = 0; i < markers.length; i++){
		markers[i].setMap(map);
	}
}

function hideMarker(marker){
	markers[i].setMap(null);
}

function hideMarkers(markers){
	for(let i = 0; i < markers.length; i++){
		markers[i].setMap(null);
	}
}

function deleteMarkers(markers){
	hideMarkers(markers);
	markers.splice(0, markers.length);
}

function initialize(){
  map = new google.maps.Map(document.getElementById('map'), {
    zoom: zoom,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  });
  infowindow = new google.maps.InfoWindow();

	setMapCenter();
	addMarkers(towers, 'tower');

	setTimeout(function(){document.getElementById('initScript').src='https://maps.googleapis.com/maps/api/js?&callback=initialize';}, 1);
}

function dirname(path) {
    return path.replace(/\\/g,'/').replace(/\/[^\/]*$/, '');;
}

function setDeviceId()
{
	var id = document.getElementById('deviceId').value.split('.');

	deleteMarkers(positions);
	nbPoints.value = 0;
	deviceId = 0;
	if((id.length > 0) && (id.length < 6)){
		for(var i = 0; i < id.length; i += 1){
			deviceId = (deviceId << (4 * (1 + (i > 1)))) +  parseInt(id[i]);
		}
	}else{
		alert('Invalid device ID');
	}
}

function refreshPage(drop=false)
{
	const login = 'toto'; //document.getElementById('login').value;
	var http = new XMLHttpRequest();
	var url = dirname(document.URL) + '/gnssDevicePositions' + (drop ? 'Delete' : 'Get');
	var parameters = new URLSearchParams({
		'deviceId': deviceId,
		'login': login,
	});
	var maxVisibleMarkersQty = document.getElementById('nbVisiblePoints').value;

	if(deviceId <= 0 || (drop && !confirm('Are you sure you want to drop all records of deviceId ' + deviceId + '?'))){
		return;
	}
	http.responseType = 'text';
	http.onreadystatechange = function () { //Call a function when the state changes.
		if (http.readyState == 4 && http.status == 200) {
			var csvPositions = http.responseText.trim().split(/\r?\n|\r/);
			var csvHeader = csvPositions[0].split(',');
			csvPositions = csvPositions.slice(1, csvPositions.length);
			var startIdx = Math.max(0, csvPositions.length - maxVisibleMarkersQty);

			if(csvPositions == ''){
				nbPoints.value = 0;
			}else{
				nbPoints.value = csvPositions.length;
			}
			if(csvPositions.length < positions.length){
				deleteMarkers(positions);
			}
			for (var n = startIdx; n < csvPositions.length; n += 1){
				var position = csvPositions[n].split(',');
				var marker = addMarker(positions, position, 'antenna');
				addInfo(marker, csvHeader, position);
			}
			startIdx = positions.length - maxVisibleMarkersQty;
			if(startIdx > 0){
				toDelete = positions.slice(0, startIdx);
				positions = positions.slice(startIdx, positions.length);
				deleteMarkers(toDelete);
			}
		}
	}
	//http.open('POST', url, true);
	//http.send(parameters.toString());
	http.open('GET', url + '?' + parameters.toString(), true);
	http.send();
}

function setMapCenter()
{
	var position = document.getElementById('mapCenter').value.split(',');

	map.setCenter(new google.maps.LatLng(position[1], position[0]));
	deleteMarkers(mapCenter);
	addMarker(mapCenter, position, 'mapCenter');
}

setInterval(refreshPage, refreshRate * 1000);
