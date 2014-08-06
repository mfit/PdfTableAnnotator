/**
 * Copyright 2014 Matthias Frey.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
/*
 * layers on by default :
 */
layerSetup.setVisibility("pdf", true);
layerSetup.setVisibility("tablebbox", true);
layerSetup.setVisibility("tables", true);

/**
 * initialise/bootstrap javascript of the project 
 */
$(document).ready(function() {
	// build controls
	pdfAnnotateBuildControls();
	
	// ready jsTable Controls
	jsTable_setControllHandlers();
	
	// ready MouseControlMouseHandler
	controlsMouseHandler = new MouseControlMouseHandler($("#pdfcontent"));	
	
	// set docid and pagen from url 
	var urlparts = window.location.href.split("?");
	if (urlparts.length == 2 && urlparts[1]!="") {
		var docparts = urlparts[1].split('/');
		if (docparts.length > 0 && ! isNaN(docparts[0])) {
			docid = parseInt(docparts[0]);
			if (docparts.length > 1 && ! isNaN(docparts[1])) {
				pagen = parseInt(docparts[1]);
			}
		}
	}
	
	setDocument(docid, pagen);
	layerSetup.setParent($("#pdfcontent"));
	layerSetup.refresh();
	
	// hide sidebar controls
	$("h1.title").click(function() {
		if ( $(".panel").css('display') == 'none') {
			$(".panel").fadeIn();
		} else {
			$(".panel").fadeOut();
		}
	});
	
	// Keyboard shortcuts
	$(document).keyup(function(e){
		
	    if (e.altKey && e.keyCode == 65) {
	    	// Alt+a , begin draw region
	    	resetArea(true, createTableControlsFromRegion);
	    	return false;
	    } 
	    if (e.keyCode == 27) {
	    	// Esc - cancel draw region
	    	resetArea(false);
	    }
	    
	    if(e.altKey && e.which==39) {
	    	// next pg.
	    	pageInc();
	    	return false;
	    }
	    
	    if(e.altKey && e.which==37) {
	    	// prev. pg.
	    	pageDec();
	    	return false;
	    }
	});
	
	//drawGrid();
}); 
