import {
  Component,
  Input,
  OnInit,
  ViewChild,
  ViewContainerRef,
  forwardRef,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { GenericSearchLayoutComponent } from '../generic-search-layout/generic-search-layout.component';
import { BaseComponent } from '../base-component';
import { EntityModelResponse, FilterModel, PagingModel } from 'dynamo/model';
import { EntityPopupDialogComponent } from '../entity-popup-dialog/entity-popup-dialog.component';
import { truncateDescriptions } from '../../functions/entitymodel-functions';
import { SelectOption } from '../../model/select-option';
import { AuthenticationService } from '../../service/authentication-service';

const MAX_ITEMS: number = 3;

/**
 * A custom component for selecting one or more items using a pop-up dialog. This can be
 * used for modifying many-to-one and many-to-many relationships
 *
 */
@Component({
  selector: 'app-lookup-field',
  templateUrl: './lookup-field.component.html',
  styleUrls: ['./lookup-field.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => LookupFieldComponent),
      multi: true,
    },
  ],
})
export class LookupFieldComponent
  extends BaseComponent
  implements ControlValueAccessor, OnInit
{
  // the name of the entity to display
  @Input({ required: true }) entityName: string = '';
  // optional reference to more specific entity model
  @Input() entityModelReference?: string;
  // entity model
  @Input() entityModel?: EntityModelResponse;

  // the name of the property that is used as the display value
  @Input({ required: true }) displayPropertyName: string = '';
  // whether selecting multiple options is allowed
  @Input({ required: true }) multiSelect: boolean = false;
  // the class to apply to the div that holds the components. set to "row" for responsive behavior by default
  @Input() rowClass: string = 'row';
  // the default filters to apply to every search
  @Input() defaultFilters: FilterModel[] = [];
  //
  @Input() class: string = 'main';
  // whether to display a quick add button
  @Input() showQuickAddButton: boolean = true;
  // whether to display a quick add button
  @Input() queryType: PagingModel.TypeEnum = PagingModel.TypeEnum.ID_BASED;

  onChange: any = () => {};
  onTouched: any = () => {};

  // the actually selected values
  selectedValues: SelectOption[] = [];
  // value cache, for holding the items selected in the popup
  valueCache: SelectOption[] = [];
  dialogVisible: boolean = false;
  disabled: boolean = false;
  selectedIds: any[] = [];
  header: string = '';

  // container for holding optional popup dialog
  @ViewChild('popupDialogContainerRef', { read: ViewContainerRef })
  vcr!: ViewContainerRef;

  @ViewChild(GenericSearchLayoutComponent) searchLayout:
    | GenericSearchLayoutComponent
    | undefined;

  constructor(private authService: AuthenticationService, translate: TranslateService) {
    super(translate);
  }

  ngOnInit(): void {}

  showDialog() {
    this.valueCache = [];
    this.selectedValues.forEach((element) => this.valueCache.push(element));

    this.selectedIds = [];
    this.selectedValues.forEach((element) =>
      this.selectedIds.push(element.value)
    );

    this.dialogVisible = true;
    this.header = this.translate.instant('search_header', { title: '' });
  }

  showQuickAddDialog() {
    let componentRef = this.vcr.createComponent(EntityPopupDialogComponent);
    componentRef.instance.entityModelReference = this.entityModelReference;
    componentRef.instance.entityName = this.entityName;
    componentRef.instance.readOnly = false;
    var callback = (obj: any): void => {
      this.afterQuickAddDialogClosed(obj);
    };
    componentRef.instance.onDialogClosed = callback;
    componentRef.instance.showDialog();
  }

  afterQuickAddDialogClosed(obj: any) {
    if (this.multiSelect === false) {
      this.selectedValues = [];
      this.selectedIds = [];
    }
    this.selectedValues.push({
      value: obj.id,
      name: obj[this.displayPropertyName],
    });
  }

  getHeader(): string {
    return this.header;
  }

  cancel(): void {
    this.dialogVisible = false;
  }

  /**
   * Closes the dialog and applies the selection
   */
  selectAndClose(): void {
    this.selectedValues = [];
    this.valueCache.forEach((element) => this.selectedValues.push(element));
    if (this.multiSelect) {
      this.onChange(this.selectedValues);
    } else {
      this.onChange(this.selectedValues[0]);
    }

    this.dialogVisible = false;
  }

  /**
   * Respond to selection of a row in the popup dialog
   * @param event the row select event (contains the entire row object)
   */
  onRowSelect(event: any) {
    if (this.multiSelect) {
      let match = this.valueCache.find((val) => val.value === event.id);
      if (!match) {
        this.valueCache.push({
          value: event.id,
          name: event[this.displayPropertyName],
        });
      }
    } else {
      this.valueCache = [];
      this.valueCache.push({
        value: event.id,
        name: event[this.displayPropertyName],
      });
    }
  }

  clear() {
    this.selectedValues = [];
    this.onChange(this.selectedValues);
  }

  getSelectedValueString() {
    if (!this.selectedValues || this.selectedValues.length == 0) {
      return undefined;
    }
    return truncateDescriptions(this.selectedValues, 'name', this.translate);
  }

  writeValue(obj: any): void {
    if (obj) {
      if (this.multiSelect) {
        this.selectedValues = obj;
      } else {
        this.selectedValues = [];
        this.selectedValues.push(obj);
      }
    } else {
      this.selectedValues = [];
    }
  }

  registerOnChange(onChange: any) {
    this.onChange = onChange;
  }

  registerOnTouched(onTouched: any): void {
    this.onTouched = onTouched;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  isQuickAddButtonVisible() {
    return this.am?.quickAddAllowed && this.showQuickAddButton && this.isWriteAllowed();
  }

  isWriteAllowed(): boolean {
    if (!this.entityModel) {
      return false;
    }

    if (!this.entityModel.writeRoles || this.entityModel.writeRoles.length == 0) {
      return true;
    };
    return this.authService.hasRole(this.entityModel.writeRoles)
  }
}
