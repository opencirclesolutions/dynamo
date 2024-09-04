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
customElements.whenDefined("vaadin-selection-grid").then(() => {
    const Grid = customElements.get("vaadin-selection-grid");
    if (Grid) {
        const oldClickHandler = Grid.prototype._onClick;
        Grid.prototype._onClick = function _click(e) {
            const boundOldClickHandler = oldClickHandler.bind(this);
            boundOldClickHandler(e);

            this._selectionGridSelectRow(e);
        };
        Grid.prototype.old_onNavigationKeyDown = Grid.prototype._onNavigationKeyDown;
        Grid.prototype._onNavigationKeyDown = function _onNavigationKeyDownOverridden(e, key) {
            this.old_onNavigationKeyDown(e,key);
            const ctrlKey = (e.metaKey)?e.metaKey:e.ctrlKey;
            if (e.shiftKey || !ctrlKey) {
                // select on shift down on shift up
                if (key === 'ArrowDown' || key === 'ArrowUp') {
                    const row = Array.from(this.$.items.children).filter(
                        (child) => child.index === this._focusedItemIndex
                    )[0];
                    if (row && typeof row.index != 'undefined') {
                        this._selectionGridSelectRowWithItem(e, row._item, row.index);
                    }
                }
            } // else do nothing
        }

        Grid.prototype.old_onSpaceKeyDown = Grid.prototype._onSpaceKeyDown;
        Grid.prototype._onSpaceKeyDown = function _onSpaceKeyDownOverriden(e) {
            this.old_onSpaceKeyDown(e);
            const tr = e.composedPath().find((p) => p.nodeName === "TR");
            if (tr && typeof tr.index != 'undefined') {
                const item = tr._item;
                const index = tr.index;
                if (this.selectedItems && this.selectedItems.some((i) => i.key === item.key)) {
                    if (this.$connector) {
                        this.$connector.doDeselection([item], true);
                    } else {
                        this.deselectItem(item);
                    }
                } else {
                    if (this.$server) {
                        this.$server.selectRangeOnly(index, index);
                    } else {
                        this.selectedItems = [];
                        this.selectItem(item);
                    }
                }
            }
        }
    }
});
