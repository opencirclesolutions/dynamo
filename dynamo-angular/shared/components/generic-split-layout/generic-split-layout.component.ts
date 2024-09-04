import {
  Component,
  EventEmitter,
  Input,
  Output,
  TemplateRef,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import {
  AttributeModelResponse,
  CRUDService,
  ModelService,
  PagingModel,
} from 'dynamo/model';
import { GenericTableComponent } from '../generic-table/generic-table.component';
import { NotificationService } from '../../service/notification-service';
import { Router } from '@angular/router';
import { GenericFormComponent } from '../generic-form/generic-form.component';
import { SplitLayoutMode } from './split-layout-mode';
import { FormBuilder, FormGroup } from '@angular/forms';
import { debounceTime } from 'rxjs';
import { FormInfo } from '../../model/form-info';
import { BaseCompositeCollectionComponent } from '../base-composite-collection-component';
import { AuthenticationService } from '../../service/authentication-service';
import { AttributeGroupMode } from '../../model/attribute-group-mode';
import { HiddenFieldService } from '../../service/hidden-field-service';

@Component({
  selector: 'app-generic-split-layout',
  templateUrl: './generic-split-layout.component.html',
  styleUrls: ['./generic-split-layout.component.scss'],
  providers: [HiddenFieldService]
})
export class GenericSplitLayoutComponent extends BaseCompositeCollectionComponent {

  // whether to open the component in view
  @Input() openInViewMode: boolean = false;
  // whether the search dialog is enabled
  @Input() searchDialogEnabled: boolean = false;
  // the quick search property name
  @Input() quickSearchProperty: string = '';
  // whether to ask for confirmation before saving
  @Input() confirmSave: boolean = false;
  // whether the form is in free form mode
  @Input() freeFormMode: boolean = false;
  // template for holding free form attributes
  @Input() freeFormTemplate?: TemplateRef<any>;
  // whether form fill functionality is enabled
  @Input() formFillEnabled: boolean = false;
  // whether form fill functionality is enabled
  @Input() attributeGroupMode: AttributeGroupMode = AttributeGroupMode.PANEL;
  // callback function to determine whether an attribute must be visible
  @Input() attributeVisible?: (am: AttributeModelResponse, editObject: any, formGroup: FormGroup) => boolean;

  // callback that is carried out after creating a new entity
  @Output() afterEntityCreated = new EventEmitter<FormInfo>();

  @ViewChild(GenericTableComponent) table?: GenericTableComponent;
  @ViewChild(GenericFormComponent) form?: GenericFormComponent;

  //container for holding optional popup dialog
  @ViewChild('popupDialogContainerRef', { read: ViewContainerRef })
  vcr!: ViewContainerRef;

  dialogVisible: boolean = false;

  searchObject: any = {};
  tableCaptionPlural: string = '';
  splitLayoutMode: SplitLayoutMode = SplitLayoutMode.EMPTY;
  selectedEntityId?: any;
  quickSearchForm?: FormGroup;

  constructor(
    service: CRUDService,
    entityModelService: ModelService,
    messageService: NotificationService,
    router: Router,
    private formBuilder: FormBuilder,
    authService: AuthenticationService,
    hiddenFieldService: HiddenFieldService
  ) {
    super(service, entityModelService, messageService, router, authService);
    this.hiddenFieldService = hiddenFieldService;
  }

  ngOnInit(): void {
    this.entityModelService
      .getEntityModel(this.entityName, this.entityModelReference)
      .subscribe((model) => {
        this.entityModel = model;
        this.tableCaptionPlural =
          this.entityModel?.displayNamesPlural[this.locale];
      });

    this.buildQuickSearchForm();
  }

  private buildQuickSearchForm() {
    if (this.quickSearchProperty) {
      this.quickSearchForm = this.formBuilder.group([]);
      let quickSearch = this.formBuilder.control(
        {
          disabled: false,
          value: undefined,
        },
        { validators: [] }
      );
      this.quickSearchForm.addControl('quickSearch', quickSearch);

      quickSearch.valueChanges.pipe(debounceTime(300)).subscribe((val) => {
        if (this.table) {
          let searchObject: any = {};
          searchObject[this.quickSearchProperty] = val;
          this.doSearch(searchObject);
          this.splitLayoutMode = SplitLayoutMode.EMPTY;
        }
      });
    }
  }

  /**
   * Method that is used to add a new entity. Opens an empty edit form
   */
  add(): void {
    if (this.table) {
      this.table.selectedIds = [];
    }

    this.splitLayoutMode = SplitLayoutMode.NEW;
    this.selectedEntityId = undefined;
    if (this.form) {
      this.form.entityId = undefined;
      this.form.viewMode = false;
      this.form.resetForm();
      this.form.setDefaultValues();
    }
  }

  /**
   * Method to respond to a click on a row on the table. Select the
   * corresponding entity in the detail layout
   * @param event the row click event
   */
  rowClick(event: any): void {
    this.splitLayoutMode = SplitLayoutMode.EXISTING;
    this.selectedEntityId = event.id;
    if (this.form) {
      this.form.customInputs! = this.customInputs;
      this.form.entityId = this.selectedEntityId;
      this.form.viewMode =
        this.openInViewMode ||
        !this.isWriteAllowed() ||
        !this.entityModel!.updateAllowed ||
        false;
      this.form.bindExistingEntity(false);
    }
  }

  /**
   * Method that is called after a save action is carried out.
   * @param event the event
   */
  afterSave(event: any): void {
    if (this.table) {
      this.table.selectedIds = [];
      this.table.selectedIds.push(event.id);
      this.splitLayoutMode == SplitLayoutMode.EXISTING;
      this.table.search();
      if (this.form) {
        this.form.viewMode =
          this.openInViewMode ||
          !this.isWriteAllowed() ||
          !this.entityModel!.updateAllowed ||
          false;
      }
    }
  }

  clearSearchFilter(): void {
    if (this.quickSearchProperty) {
      this.quickSearchForm!.get('quickSearch')?.reset();
    }
    this.doSearch({});
  }

  /**
   * Method that is called after executing a search in the search dialog
   * @param event
   */
  afterSearchDialogSearch(event: any): void {
    if (this.quickSearchProperty) {
      this.quickSearchForm!.get('quickSearch')?.reset(undefined, {
        emitEvent: false,
      });
    }
    this.doSearch(event);
  }

  doSearch(searchObject: any): void {
    if (this.table) {
      this.table!.searchObject = searchObject;
      this.table!.search();
    }
  }

  protected override onLookupFilled(am: AttributeModelResponse): void {
    // no action necessary
  }

  search(): void {
    this.doSearch(this.searchObject)
  }

  cancelDialog(): void {
    this.dialogVisible = false;
  }

  selectAndClose(): void {
    this.dialogVisible = false;
  }

  openSearchDialog(): void {
    this.dialogVisible = true;
  }

  isEmptyMode(): boolean {
    return this.splitLayoutMode == SplitLayoutMode.EMPTY;
  }

  isNewMode(): boolean {
    return this.splitLayoutMode == SplitLayoutMode.NEW;
  }

  isExistingMode(): boolean {
    return this.splitLayoutMode == SplitLayoutMode.EXISTING;
  }

  onEntityCreated(event: any) {
    this.afterEntityCreated.emit(event);
  }

  override afterActionDialogClosed(): void {
    this.doSearch(this.searchObject);
  }
}
