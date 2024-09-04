import {
  Component,
  ContentChildren,
  Input,
  OnChanges,
  QueryList,
  SimpleChanges,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import {
  AttributeModelResponse,
  CRUDService,
  ModelService,
  PagingModel,
} from 'dynamo/model';
import { PopupButtonMode } from '../../model/popup-button-mode';
import { GenericTableComponent } from '../generic-table/generic-table.component';
import { NotificationService } from '../../service/notification-service';
import { Router } from '@angular/router';
import { EntityPopupDialogComponent } from '../entity-popup-dialog/entity-popup-dialog.component';
import { FormGroup } from '@angular/forms';
import { BaseCompositeCollectionComponent } from '../base-composite-collection-component';
import { AuthenticationService } from '../../service/authentication-service';
import { HiddenFieldService } from '../../service/hidden-field-service';

/**
 * A layout consisting of a table that contains search results
 */
@Component({
  selector: 'app-generic-edit-table-layout',
  templateUrl: './generic-edit-table-layout.component.html',
  styleUrls: ['./generic-edit-table-layout.component.scss'],
  providers: [HiddenFieldService]
})
export class GenericEditTableLayoutComponent
  extends BaseCompositeCollectionComponent
  implements OnChanges
{
  // additional validation function to carry out before submitting the form
  @Input() additionalValidation?: (formGroup: FormGroup) => string | undefined;

  @ViewChild(GenericTableComponent) table?: GenericTableComponent;

  //container for holding optional popup dialog
  @ViewChild('popupDialogContainerRef', { read: ViewContainerRef })
  vcr!: ViewContainerRef;

  defaultPageSize = 10;
  searchObject: any = {};
  tableCaptionPlural: string = '';
  loading: boolean = false;
  popupButtonMode: PopupButtonMode = PopupButtonMode.EDIT;

  constructor(
    service: CRUDService,
    entityModelService: ModelService,
    messageService: NotificationService,
    router: Router,
    authService: AuthenticationService,
    hiddenFieldService: HiddenFieldService
  ) {
    super(service, entityModelService, messageService, router, authService);
    this.hiddenFieldService = hiddenFieldService;
  }

  ngOnChanges(changes: SimpleChanges): void {}

  ngOnInit(): void {
    this.entityModelService
      .getEntityModel(this.entityName, this.entityModelReference)
      .subscribe({
        next: (model) => {
          this.entityModel = model;
          this.tableCaptionPlural =
            this.entityModel.displayNamesPlural[this.locale];
        },
        error: (error) => this.messageService.error(error.error?.message),
      });
  }

  onSearch(searchObject: any) {
    this.searchObject = searchObject;
    this.loading = true;
    if (this.table) {
      this.table.searchObject = searchObject!;
      this.table.search();
    }
  }

  search() {
    if (this.table) {
      this.table.defaultFilters = this.defaultFilters;
      this.table!.search();
    }
  }

  searchComplete() {
    this.loading = false;
  }

  protected override onLookupFilled(am: AttributeModelResponse): void {
    // no action necessary
  }

  /**
   * Displays a popup dialog containing the selected entity
   * @param row the selected entity
   */
  openNewEntityDialog(): void {
    let componentRef = this.vcr!.createComponent(EntityPopupDialogComponent);

    componentRef.instance.entityModel = this.entityModel;
    componentRef.instance.entityModelReference = this.entityModelReference;
    componentRef.instance.entityName = this.entityName;
    componentRef.instance.fieldFilters = this.fieldFilters;
    componentRef.instance.additionalActions = this.additionalFormActions;
    componentRef.instance.postProcessInputForm = this.postProcessInputForm;
    componentRef.instance.additionalValidation = this.additionalValidation;
    componentRef.instance.customValidatorTemplate = this.customValidatorTemplate;
    componentRef.instance.injectedCustomInputs = this.customInputs;
    componentRef.instance.injectedHiddenFieldService = this.hiddenFieldService;
    componentRef.instance.readOnly = false;

    var callback = (obj: any): void => {
      this.search();
    };
    componentRef.instance.onDialogClosed = callback;
    componentRef.instance.showDialog();
  }
}
