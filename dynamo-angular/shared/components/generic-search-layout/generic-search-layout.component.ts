import {
  Component,
  EventEmitter,
  Input,
  Output,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import {
  AttributeModelResponse,
  CRUDService,
  ModelService,
  PagingModel,
} from 'dynamo/model';
import { NotificationService } from '../../service/notification-service';
import { Router } from '@angular/router';
import { GenericTableComponent } from '../generic-table/generic-table.component';
import { PopupButtonMode } from '../../model/popup-button-mode';
import { SearchFormMode } from '../../model/search-form-mode';
import { FormInfo } from '../../model/form-info';
import { BaseCompositeCollectionComponent } from '../base-composite-collection-component';
import { AuthenticationService } from '../../service/authentication-service';

/**
 * A composite component that consists of a search form and a results table
 */
@Component({
  selector: 'app-generic-search-layout',
  templateUrl: './generic-search-layout.component.html',
  styleUrls: ['./generic-search-layout.component.scss'],
})
export class GenericSearchLayoutComponent extends BaseCompositeCollectionComponent {

  // the key under which to store data in the state store key
  @Input() stateStoreKey?: string = undefined;
  // the IDs of the selected rows
  @Input() selectedIds: any[] = [];
  // whether multiple select is allowed. this is used when this layout is used inside a search dialog
  @Input() multiSelect: boolean = false;
  // whether this component is nested inside another component (notable a search dialog)
  @Input() nested: boolean = false;
  // whether to ask for confirmation before clearing the search form
  @Input() confirmClear: boolean = false;
  // whether to preserve the search terms between page visits
  @Input() preserveSearchTerms: boolean = true;
  // whether advance search mode is enabled
  @Input() advancedModeEnabled: boolean = false;
  // whether to search immediately after opening the screen
  @Input() searchImmediately: boolean = true;
  // whether it is possible to navigate to the detail screen
  @Input() detailsModeEnabled: boolean = true;
  // whether to display the button to navigate to the detail screen in the grid
  @Input() showDetailButton: boolean = false;
  // the search form mode
  @Input() searchFormMode: SearchFormMode = SearchFormMode.STANDARD;
  // popup button mode
  @Input() popupButtonMode: PopupButtonMode = PopupButtonMode.NONE;
  // detail navigation path
  @Input() detailNavigationPath: string = '';

  // event handler that is called when the a row is selected in the table
  @Output() onRowSelect: EventEmitter<any> = new EventEmitter();
  // event handler that is called when the add button is clicked
  @Output() onAddButtonClick: EventEmitter<any> = new EventEmitter();
  // event handler that is called after a search is performed
  @Output() afterSearchPerformed: EventEmitter<any> = new EventEmitter();
  // event handler that is called after the search form has been built. Use to set defaults
  @Output() afterSearchFormBuilt: EventEmitter<any> =
    new EventEmitter<FormInfo>();

    // nested table component
  @ViewChild(GenericTableComponent) table?: GenericTableComponent;
  //container for holding optional popup dialog
  @ViewChild('popupDialogContainerRef', { read: ViewContainerRef })
  vcr!: ViewContainerRef;


  defaultPageSize = 10;
  searchObject: any = {};
  tableCaptionPlural: string = '';
  loading: boolean = !this.searchImmediately;

  constructor(
    service: CRUDService,
    entityModelService: ModelService,
    messageService: NotificationService,
    router: Router,
    authService: AuthenticationService,
  ) {
    super(service, entityModelService, messageService, router, authService);
  }

  ngOnInit(): void {
    this.entityModelService
      .getEntityModel(this.entityName, this.entityModelReference)
      .subscribe({
        next: (model) => {
          this.entityModel = model;
          this.tableCaptionPlural =
            this.entityModel.displayNamesPlural[this.locale];
        },
        error: (error) => error?.error ?
          this.messageService.error(error.error?.message) :
          this.messageService.error("Unknown error")
      });
  }

  onSearch(searchObject: any) {
    this.searchObject = searchObject;
    this.loading = true;
    if (this.table) {
      this.table.searchObject = searchObject;
      this.table.search();
    }
    this.afterSearchPerformed.emit(searchObject);
  }

  onReset(event: any) {
    if (this.table) {
      this.table.reset();
    }
  }

  search() {
    this.table!.search();
  }

  /**
   * Respond to a click on the "add"-button. Will by default
   * navigate to a detail page
   */
  add() {
    if (this.detailNavigationPath) {
      this.router.navigateByUrl(this.detailNavigationPath);
    } else {
      this.onAddButtonClick.emit();
    }
  }

  /**
   * Respond to a click on a row in the table. Will by default
   * navigate to a detail page
   */
  rowClick(event: any) {
    if (event.id && this.detailNavigationPath) {
      this.router.navigateByUrl(this.detailNavigationPath + '/' + event.id);
    } else {
      this.onRowSelect.emit(event);
    }
  }

  searchComplete() {
    this.loading = false;
  }

  isStandard() {
    return this.searchFormMode == SearchFormMode.STANDARD;
  }

  protected override onLookupFilled(am: AttributeModelResponse): void {
    // no action necessary
  }

  doAfterSearchFormBuilt(info: FormInfo) {
    this.afterSearchFormBuilt.emit(info);
  }

  override afterActionDialogClosed(): void {
      this.search();
  }

}
