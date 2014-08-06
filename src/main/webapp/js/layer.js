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

/**
 * Layers - layer management,( "contentLayer" & "contentLayers"
 * 
 * - help with displaying document information in the workspace
 * 
 * - the layers need to be updated when 
 * 			- the document and/or page changes
 * 			- the scale factor is changed
 * 			- certain layers on other actions (like a table is drawn/changed ... ) 
 * 
 * - the visibility of each layer can be changed
 * 
 */


/**
 * represents one layer of information
 * name - name of layer
 * urlparts - returns an url . can also do the whole of the work.
 * data_callback - called with respons form url (if an url is returned)
 */
var contentLayer = function(name, urlparts, data_callback, settings) {
	this.name = name;
	this.parts = urlparts;
	this.callback = data_callback;
	this.parent = "";
	this.element = "div";
	this.settings = {}
	if (settings!=undefined) {
		this.settings = settings;
	}
	if (! 'caption' in settings) {
		settings['caption'] = name;
	} 
}

/**
 * hide / remove the visual repr. of the layer
 */
contentLayer.prototype.hideLayer = function() {
	$("#pdfcontent-layer-" + this.name, this.parent).remove();
}

contentLayer.prototype.initLayer = function() { 
}

/**
 * build / refresh layer - usually that means :
 * query rest-service, transform the resulting data into dom elements
 */
contentLayer.prototype.buildLayer = function () {
	url = this.parts({docid:docid, page:pagen, scale:outscale})
	var layer = this;
	if (url) {
		// we need to get the url and call the callback provided 
		$.get(url, function(data) {
			$("#pdfcontent-layer-" + layer.name, layer.parent).remove();
			var add_dom = layer.callback(data);
			add_dom.attr('id', "pdfcontent-layer-"+layer.name);
			add_dom.addClass('pdfcontent-layer');
			layer.parent.append(add_dom); 
			
			// set z-index
			if ( layer.settings["pri"] != undefined) {
				add_dom.css('z-index', layer.settings["pri"]);
			}
		});
	}
}


/**
 * collection of layers
 */
var contentLayers = function() {
	this.parent;
	this.layers = {}
	this.layer_names = []
	this.visible_layers = [];
	this.dirty_layers = [];
}


/**
 * add a layer to the collection
 */
contentLayers.prototype.registerLayer = function(name, parts, callback) {
	this.layers[name] = parts;
	this.layer_names.push(name);
	return this;
}

/**
 * set parent element in which layers should be created 
 */
contentLayers.prototype.setParent = function(el) {
	this.parent = el;
	for (i in this.layers) this.layers[i].parent = el;
	return this;
}

/**
 * draw all visible layers
 */
contentLayers.prototype.refresh = function() {
	var dlen = this.dirty_layers.length;
	for (var i = 0; i<dlen; i++) {
		this.layers[this.dirty_layers[i]].hideLayer();
		if (this.visible_layers.indexOf(this.dirty_layers[i]) != -1) {
			this.layers[this.dirty_layers[i]].buildLayer();	
		}
	}
	this.dirty_layers = [];
	return this;
}

/**
 * change visibility of layer
 */
contentLayers.prototype.setVisibility = function(lname, visstat) {
	if (this.layer_names.indexOf(lname)!=-1) {
		if (visstat) {
			// show
			if (this.visible_layers.indexOf(lname)==-1) {
				this.visible_layers.push(lname);
				this.dirty_layers.push(lname);
			}
		} else {
			// hide
			var ind;
			if ((ind = this.visible_layers.indexOf(lname))!=-1) {
				this.visible_layers.splice(ind,1);
				this.dirty_layers.push(lname);
			}
		}
	}
	return this;
}

/**
 * return layer visibility state
 */
contentLayers.prototype.getVisibility = function(lname) {
	return this.visible_layers.indexOf(lname)!=-1;
}

/**
 * mark layer for redrawing
 */
contentLayers.prototype.setDirty = function(lname) {
	if (this.layer_names.indexOf(lname)!=-1) {
		if (this.dirty_layers.indexOf(lname)==-1) {
			this.dirty_layers.push(lname);
		}
	}
	return this;
}

/**
 * mark all for redrawing
 */
contentLayers.prototype.setAllDirty = function() {
	var vlen = this.visible_layers.length;
	for(var i=0; i<vlen; i++) {
		this.setDirty(this.visible_layers[i]);
	}
	return this;
}
