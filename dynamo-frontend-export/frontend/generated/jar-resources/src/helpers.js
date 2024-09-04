/*-
 * #%L
 * Selection Grid
 * %%
 * Copyright (C) 2020 Vaadin Ltd
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/* eslint-disable no-invalid-this */

export function _selectionGridSelectRow(e) {
    const vaadinTreeToggle = e.composedPath().find((p) => p.nodeName === "VAADIN-GRID-TREE-TOGGLE");
    if (vaadinTreeToggle) {
        // don't select, it will expand/collapse the node
        // reset the last item
        this.rangeSelectRowFrom = -1;
    } else {
        const tr = e.composedPath().find((p) => p.nodeName === "TR");
        if (tr && typeof tr.index != 'undefined') {
            const item = tr._item;
            const index = tr.index;

            this._selectionGridSelectRowWithItem(e, item, index);
        }
    }
}
export function _selectionGridSelectRowWithItem(e, item, index) {
    const ctrlKey = (e.metaKey)?e.metaKey:e.ctrlKey; //(this._ios)?e.metaKey:e.ctrlKey;
    // if click select only this row
    if (!ctrlKey && !e.shiftKey) {
        if (this.$server) {
            this.$server.selectRangeOnly(index, index);
        } else {
            this.selectedItems = [];
            this.selectItem(item);
        }
    }
    // if ctrl click
    if (e.shiftKey && this.rangeSelectRowFrom >= 0) {
        if((this.rangeSelectRowFrom - index) !== 0) { // clear text selection, if multiple rows are selected using shift
            const sel = window.getSelection ? window.getSelection() : document.selection;
            if (sel) {
                if (sel.removeAllRanges) {
                    sel.removeAllRanges();
                } else if (sel.empty) {
                    sel.empty();
                }
            }
        }

        if (!ctrlKey) {
            if (this.$server) {
                this.$server.selectRangeOnly(this.rangeSelectRowFrom, index);
            }
        } else {
            if (this.$server) {
                this.$server.selectRange(this.rangeSelectRowFrom, index);
            }
        }
    } else {
        if (!ctrlKey) {
            if (this.$server) {
                this.$server.selectRangeOnly(index, index);
            }
        } else {
            if (this.selectedItems && this.selectedItems.some((i) => i.key === item.key)) {
                if (this.$connector) {
                    this.$connector.doDeselection([item], true);
                } else {
                    this.deselectItem(item);
                }
            } else {
                if (this.$server) {
                    this.$server.selectRange(index, index);
                }
            }
        }
        this.rangeSelectRowFrom = index;
    }
}

export function _getItemOverriden(index, el) {
    if (index >= this._effectiveSize) {
        return;
    }
    el.index = index;
    const { cache, scaledIndex } = this._cache.getCacheAndIndex(index);
    const item = cache.items[scaledIndex];
    if (item) {
        this.__updateLoading(el, false);
        this._updateItem(el, item);
        if (this._isExpanded(item)) {
            cache.ensureSubCacheForScaledIndex(scaledIndex);
        }
    } else {
        this.__updateLoading(el, true);
        const page = Math.floor(scaledIndex / this.pageSize);
        this._loadPage(page, cache);
    }
    /** focus when get item if there is an item to focus **/
    if (this._rowNumberToFocus > -1) {
        if (index === this._rowNumberToFocus) {
            const row = Array.from(this.$.items.children).filter(
                (child) => child.index === this._rowNumberToFocus
            )[0];
            if (row) {
                this._focus();
            }
        }
    }
}
