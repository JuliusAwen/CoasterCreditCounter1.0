package de.juliusawen.coastercreditcounter.userInterface.activities;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import java.util.List;
import java.util.UUID;

import de.juliusawen.coastercreditcounter.R;
import de.juliusawen.coastercreditcounter.application.App;
import de.juliusawen.coastercreditcounter.application.Constants;
import de.juliusawen.coastercreditcounter.dataModel.elements.IElement;
import de.juliusawen.coastercreditcounter.dataModel.elements.Park;
import de.juliusawen.coastercreditcounter.dataModel.elements.attractions.CustomAttraction;
import de.juliusawen.coastercreditcounter.dataModel.elements.attractions.IAttraction;
import de.juliusawen.coastercreditcounter.dataModel.elements.properties.Category;
import de.juliusawen.coastercreditcounter.dataModel.elements.properties.CreditType;
import de.juliusawen.coastercreditcounter.dataModel.elements.properties.Manufacturer;
import de.juliusawen.coastercreditcounter.dataModel.elements.properties.Status;
import de.juliusawen.coastercreditcounter.tools.DrawableProvider;
import de.juliusawen.coastercreditcounter.tools.ResultFetcher;
import de.juliusawen.coastercreditcounter.tools.Toaster;
import de.juliusawen.coastercreditcounter.tools.activityDistributor.ActivityDistributor;
import de.juliusawen.coastercreditcounter.tools.activityDistributor.RequestCode;

public class CreateOrEditAttractionActivity extends BaseActivity
{
    private CreateOrEditAttractionActivityViewModel viewModel;

    private LinearLayout layoutCreditType;
    private LinearLayout layoutCategory;
    private LinearLayout layoutManufacturer;
    private LinearLayout layoutStatus;

    private EditText editTextAttractionName;

    private TextView textViewCreditType;
    private ImageView imageViewCreditType;

    private TextView textViewCategory;
    private ImageView imageViewCategory;

    private TextView textViewManufacturer;
    private ImageView imageViewManufacturer;

    private TextView textViewStatus;
    private ImageView imageViewStatus;

    private EditText editTextUntrackedRideCount;

    protected void setContentView()
    {
        setContentView(R.layout.activity_create_or_edit_custom_attraction);
    }

    protected void create()
    {
        this.editTextAttractionName = findViewById(R.id.editTextCreateOrEditAttractionName);

        this.textViewCreditType = findViewById(R.id.textViewCreateOrEditAttraction_CreditType);
        this.imageViewCreditType = findViewById(R.id.imageViewCreateOrEditAttraction_CreditType);

        this.textViewCategory = findViewById(R.id.textViewCreateOrEditAttraction_Category);
        this.imageViewCategory = findViewById(R.id.imageViewCreateOrEditAttraction_Category);

        this.textViewManufacturer = findViewById(R.id.textViewCreateOrEditAttraction_Manufacturer);
        this.imageViewManufacturer = findViewById(R.id.imageViewCreateOrEditAttraction_Manufacturer);

        this.textViewStatus = findViewById(R.id.textViewCreateOrEditAttraction_Status);
        this.imageViewStatus = findViewById(R.id.imageViewCreateOrEditAttraction_Status);

        this.editTextUntrackedRideCount = findViewById(R.id.editTextCreateOrEditAttractionUntrackedRideCount);

        this.viewModel = new ViewModelProvider(this).get(CreateOrEditAttractionActivityViewModel.class);

        if(RequestCode.values()[getIntent().getIntExtra(Constants.EXTRA_REQUEST_CODE, 0)] == RequestCode.EDIT_ATTRACTION)
        {
            this.viewModel.isEditMode = true;
        }

        if(this.viewModel.isEditMode) //adjust to work with Blueprints
        {
            if(this.viewModel.attraction == null)
            {
                this.viewModel.attraction = (IAttraction) App.content.getContentByUuid(UUID.fromString(getIntent().getStringExtra(Constants.EXTRA_ELEMENT_UUID)));
                this.viewModel.parentPark = (Park) this.viewModel.attraction.getParent();
            }
        }
        else if(this.viewModel.parentPark == null) //adjust to work with ManageBlueprints
        {
            this.viewModel.parentPark = (Park) App.content.getContentByUuid(UUID.fromString(getIntent().getStringExtra(Constants.EXTRA_ELEMENT_UUID)));
        }

        if(this.viewModel.attraction != null) //edit mode
        {
            if(this.viewModel.attraction.isCustomAttraction())
            {
                //this.decorateViewForEditCustomAttraction();
                this.createLayoutCreditType();
                this.createLayoutCategory();
                this.createLayoutManufacturer();
            }
            else if(this.viewModel.attraction.isStockAttraction())
            {
                //this.decorateViewForEditStockAttraction();
            }
            else if(this.viewModel.attraction.isBlueprint())
            {
                //this.decorateViewForEditBlueprint();
            }
            else
            {
                //log error
            }
            this.createLayoutStatus();
        }
        else //create mode
        {
            //distuingish between create createAttraction and CreateBlueprint (via RequestCode?)
            this.createLayoutCreditType();
            this.createLayoutCategory();
            this.createLayoutManufacturer();
            this.createLayoutStatus();
        }

        this.createEditTextUntrackedRideCount();




        if(this.viewModel.toolbarTitle == null)
        {
            this.viewModel.toolbarTitle = this.viewModel.isEditMode ? getIntent().getStringExtra(Constants.EXTRA_TOOLBAR_TITLE) : getString(R.string.title_create_attraction);
        }

        if(this.viewModel.toolbarSubtitle == null)
        {
            this.viewModel.toolbarSubtitle = this.viewModel.isEditMode
                    ? this.viewModel.attraction.getName()
                    : getString(R.string.subtitle_create_attraction, this.viewModel.parentPark.getName());
        }

        super.addHelpOverlayFragment(
                getString(R.string.title_help, this.viewModel.isEditMode
                        ? getIntent().getStringExtra(Constants.EXTRA_TOOLBAR_TITLE)
                        : getString(R.string.title_create_attraction)),
                getText(R.string.help_text_create_or_edit_attraction));
        super.addToolbar();
        super.addToolbarHomeButton();
        super.setToolbarTitleAndSubtitle(this.viewModel.toolbarTitle, this.viewModel.toolbarSubtitle);
        super.addFloatingActionButton();

        this.decorateFloatingActionButton();
        this.createEditTextAttractionName();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(Constants.LOG_TAG, String.format("CreateOrEditAttractionActivity.onActivityResult:: requestCode[%s], resultCode[%s]", requestCode, resultCode));

        if(resultCode != RESULT_OK)
        {
            return;
        }

        IElement pickedElement = ResultFetcher.fetchResultElement(data);
        if(pickedElement != null)
        {
            switch(RequestCode.getValue(requestCode))
            {
                case MANAGE_CREDIT_TYPES:
                case PICK_CREDIT_TYPE:
                    this.setText((CreditType)pickedElement);
                    break;

                case MANAGE_CATEGORIES:
                case PICK_CATEGORY:
                    this.setText((Category)pickedElement);
                    break;

                case MANAGE_MANUFACTURERS:
                case PICK_MANUFACTURER:
                    this.setText((Manufacturer)pickedElement);
                    break;

                case MANAGE_STATUSES:
                case PICK_STATUS:
                    this.setText((Status)pickedElement);
                    break;
            }
            Log.i(Constants.LOG_TAG, String.format("CreateOrEditAttractionActivity.onActivityResult:: picked %s", pickedElement));
        }
    }

    private void setText(CreditType element)
    {
        this.textViewCreditType.setText(element.getName());
        this.viewModel.creditType = element;
    }

    private void setText(Category element)
    {
        this.textViewCategory.setText(element.getName());
        this.viewModel.category = element;
    }

    private void setText(Manufacturer element)
    {
        this.textViewManufacturer.setText(element.getName());
        this.viewModel.manufacturer = element;
    }

    private void setText(Status element)
    {
        this.textViewStatus.setText(element.getName());
        this.viewModel.status = element;
    }

    private void decorateFloatingActionButton()
    {
        super.setFloatingActionButtonIcon(DrawableProvider.getColoredDrawable(R.drawable.ic_baseline_check, R.color.white));
        super.setFloatingActionButtonOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                boolean somethingWentWrong = false;

                viewModel.name = editTextAttractionName.getText().toString();
                Log.v(Constants.LOG_TAG, String.format("CreateOrEditAttractionActivity.onClickFab:: attraction name entered [%s]", viewModel.name));

                String untrackedRideCountString = editTextUntrackedRideCount.getText().toString();
                try
                {
                    if(!untrackedRideCountString.trim().isEmpty())
                    {
                        viewModel.untrackedRideCount = Integer.parseInt(untrackedRideCountString);
                    }
                    else
                    {
                        viewModel.untrackedRideCount = 0;
                    }
                    Log.v(Constants.LOG_TAG, String.format("CreateOrEditAttractionActivity.onClickFab:: untracked ride count set to [%d]", viewModel.untrackedRideCount));
                }
                catch(NumberFormatException nfe)
                {
                    Log.w(Constants.LOG_TAG, String.format("CreateOrEditAttractionActivity.onClickFab:: catched NumberFormatException parsing untracked ride count: [%s]", nfe));

                    somethingWentWrong = true;
                    Toaster.makeShortToast(CreateOrEditAttractionActivity.this, getString(R.string.error_number_not_valid));
                }


                if(!somethingWentWrong)
                {
                    if(viewModel.isEditMode)
                    {
                        boolean somethingChanged = false;

                        if(!viewModel.attraction.getName().equals(viewModel.name))
                        {
                            if(viewModel.attraction.setName(viewModel.name))
                            {
                                somethingChanged = true;
                            }
                            else
                            {
                                somethingWentWrong = true;
                                Toaster.makeShortToast(CreateOrEditAttractionActivity.this, getString(R.string.error_name_not_valid));
                            }
                        }

                        if(viewModel.attraction.hasCreditType())
                        {
                            if(!viewModel.attraction.getCreditType().equals(viewModel.creditType))
                            {
                                viewModel.attraction.setCreditType(viewModel.creditType);
                                somethingChanged = true;
                            }
                        }

                        if(viewModel.attraction.hasCategory())
                        {
                            if(!viewModel.attraction.getCategory().equals(viewModel.category))
                            {
                                viewModel.attraction.setCategory(viewModel.category);
                                somethingChanged = true;
                            }
                        }

                        if(viewModel.attraction.hasManufacturer())
                        {
                            if(!viewModel.attraction.getManufacturer().equals(viewModel.manufacturer))
                            {
                                viewModel.attraction.setManufacturer(viewModel.manufacturer);
                                somethingChanged = true;
                            }
                        }

                        if(viewModel.attraction.hasStatus())
                        {
                            if(!viewModel.attraction.getStatus().equals(viewModel.status))
                            {
                                viewModel.attraction.setStatus(viewModel.status);
                                somethingChanged = true;
                            }
                        }

                        if(viewModel.attraction.getUntracktedRideCount() != viewModel.untrackedRideCount)
                        {
                            viewModel.attraction.setUntracktedRideCount(viewModel.untrackedRideCount);
                            somethingChanged = true;
                        }

                        if(somethingChanged && !somethingWentWrong)
                        {
                            markForUpdate(viewModel.attraction);
                        }

                        if(!somethingWentWrong)
                        {
                            if(somethingChanged)
                            {
                                returnResult(RESULT_OK);
                            }
                            else
                            {
                                returnResult(RESULT_CANCELED);
                            }
                        }
                    }
                    else
                    {
                        if(createAttraction())
                        {
                            Log.d(Constants.LOG_TAG, String.format("CreateOrEditAttractionActivity.onClickFab:: adding child %s to parent %s", viewModel.attraction, viewModel.parentPark));

                            viewModel.parentPark.addChildAndSetParent(viewModel.attraction);

                            CreateOrEditAttractionActivity.super.markForCreation(viewModel.attraction);
                            CreateOrEditAttractionActivity.super.markForUpdate(viewModel.parentPark);

                            returnResult(RESULT_OK);
                        }
                        else
                        {
                            Toaster.makeShortToast(CreateOrEditAttractionActivity.this, getString(R.string.error_name_not_valid));
                        }
                    }
                }
            }
        });

        super.setFloatingActionButtonVisibility(true);
    }

    private void createEditTextAttractionName()
    {
        this.editTextAttractionName.setOnEditorActionListener(this.getOnEditorActionListener());
        if(this.viewModel.isEditMode)
        {
            this.editTextAttractionName.setText(this.viewModel.attraction.getName());
            this.editTextAttractionName.setSelection(this.viewModel.attraction.getName().length());
        }
    }

    private void createLayoutCreditType()
    {
        this.layoutCreditType = findViewById(R.id.linearLayoutCreateOrEditAttraction_CreditType);
        this.layoutCreditType.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(Constants.LOG_TAG, "CreateOrEditAttractionActivity.onClick:: <PickCreditType> selected");

                List<IElement> elements = App.content.getContentOfType(CreditType.class);

                if(elements.size() == 1)
                {
                    Log.d(Constants.LOG_TAG, "CreateOrEditAttractionActivity.onClick:: only one element found - picked!");
                    setText((CreditType)elements.get(0));
                }
                else
                {
                    ActivityDistributor.startActivityPickForResult(CreateOrEditAttractionActivity.this, RequestCode.PICK_CREDIT_TYPE, elements);
                }
            }
        });

        CreditType creditType = this.viewModel.isEditMode ? this.viewModel.attraction.getCreditType() : CreditType.getDefault();
        this.textViewCreditType.setText(creditType.getName());

        this.imageViewCreditType.setImageDrawable(DrawableProvider.getColoredDrawable(R.drawable.ic_baseline_build, R.color.black));
        this.imageViewCreditType.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ActivityDistributor.startActivityManageForResult(CreateOrEditAttractionActivity.this, RequestCode.MANAGE_CREDIT_TYPES);
            }
        });

        this.viewModel.creditType = creditType;
    }

    private void createLayoutCategory()
    {
        this.layoutCategory = findViewById(R.id.linearLayoutCreateOrEditAttraction_Category);
        this.layoutCategory.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(Constants.LOG_TAG, "CreateOrEditAttractionActivity.onClick:: <PickCategory> selected");

                List<IElement> elements = App.content.getContentOfType(Category.class);

                if(elements.size() == 1)
                {
                    Log.d(Constants.LOG_TAG, "CreateOrEditAttractionActivity.onClick:: only one element found - picked!");
                    setText((Category)elements.get(0));
                }
                else
                {

                    ActivityDistributor.startActivityPickForResult(CreateOrEditAttractionActivity.this, RequestCode.PICK_CATEGORY, elements);
                }
            }
        });

        Category category = this.viewModel.isEditMode ? this.viewModel.attraction.getCategory() : Category.getDefault();
        this.textViewCategory.setText(category.getName());

        this.imageViewCategory.setImageDrawable(DrawableProvider.getColoredDrawable(R.drawable.ic_baseline_build, R.color.black));
        this.imageViewCategory.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ActivityDistributor.startActivityManageForResult(CreateOrEditAttractionActivity.this, RequestCode.MANAGE_CATEGORIES);
            }
        });

        this.viewModel.category = category;
    }

    private void createLayoutManufacturer()
    {
        this.layoutManufacturer = findViewById(R.id.linearLayoutCreateOrEditAttraction_Manufacturer);
        this.layoutManufacturer.setOnClickListener((new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(Constants.LOG_TAG, "CreateOrEditAttractionActivity.onClick:: <PickManufacturer> selected");

                List<IElement> elements = App.content.getContentOfType(Manufacturer.class);

                if(elements.size() == 1)
                {
                    Log.d(Constants.LOG_TAG, "CreateOrEditAttractionActivity.onClick:: only one element found - picked!");
                    setText((Manufacturer)elements.get(0));
                }
                else
                {

                    ActivityDistributor.startActivityPickForResult(CreateOrEditAttractionActivity.this, RequestCode.PICK_MANUFACTURER, elements);
                }
            }
        }));

        Manufacturer manufacturer = this.viewModel.isEditMode ? this.viewModel.attraction.getManufacturer() : Manufacturer.getDefault();
        this.textViewManufacturer.setText(manufacturer.getName());

        this.imageViewManufacturer.setImageDrawable(DrawableProvider.getColoredDrawable(R.drawable.ic_baseline_build, R.color.black));
        this.imageViewManufacturer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ActivityDistributor.startActivityManageForResult(CreateOrEditAttractionActivity.this, RequestCode.MANAGE_MANUFACTURERS);
            }
        });

        this.viewModel.manufacturer = manufacturer;
    }

    private void createLayoutStatus()
    {
        this.layoutStatus = findViewById(R.id.linearLayoutCreateOrEditAttraction_Status);
        this.layoutStatus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(Constants.LOG_TAG, "CreateOrEditAttractionActivity.onClick:: <PickStatus> selected");

                List<IElement> elements = App.content.getContentOfType(Status.class);

                if(elements.size() == 1)
                {
                    Log.d(Constants.LOG_TAG, "CreateOrEditAttractionActivity.onClick:: only one element found - picked!");
                    setText((Status)elements.get(0));
                }
                else
                {
                    ActivityDistributor.startActivityPickForResult(CreateOrEditAttractionActivity.this, RequestCode.PICK_STATUS, elements);
                }
            }
        });

        Status status = this.viewModel.isEditMode ? this.viewModel.attraction.getStatus() : Status.getDefault();
        this.textViewStatus.setText(status.getName());

        this.imageViewStatus.setImageDrawable(DrawableProvider.getColoredDrawable(R.drawable.ic_baseline_build, R.color.black));
        this.imageViewStatus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ActivityDistributor.startActivityManageForResult(CreateOrEditAttractionActivity.this, RequestCode.MANAGE_STATUSES);
            }
        });

        this.viewModel.status = status;
    }

    private void createEditTextUntrackedRideCount()
    {
        this.editTextUntrackedRideCount.setOnEditorActionListener(this.getOnEditorActionListener());
        this.editTextUntrackedRideCount.setText(this.viewModel.isEditMode
                ? String.valueOf(this.viewModel.attraction.getUntracktedRideCount())
                : String.valueOf(0));
    }

    private TextView.OnEditorActionListener getOnEditorActionListener()
    {
        return new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event)
            {
                Log.i(Constants.LOG_TAG, String.format("CreateOrEditAttractionActivity.getOnEditorActionListener.onClickEditorAction:: actionId[%d]", actionId));

                boolean handled = false;

                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    InputMethodManager inputMethodManager = (InputMethodManager) textView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    textView.clearFocus();
                    handled = true;
                }

                return handled;
            }
        };
    }

    private boolean createAttraction() //adjust to make it work for Blueprints
    {
        boolean success = false;
        IAttraction attraction = CustomAttraction.create(this.editTextAttractionName.getText().toString(), this.viewModel.untrackedRideCount);

//        IAttraction attraction = Blueprint.create(this.editTextAttractionName.getText().toString());
//
//        IAttraction attraction = StockAttraction.create(this.editTextAttractionName.getText().toString(), Blueprint.create("unnamed"));

        if(attraction != null)
        {
            this.viewModel.attraction = attraction;
            this.viewModel.attraction.setCreditType(this.viewModel.creditType);
            this.viewModel.attraction.setCategory(this.viewModel.category);
            this.viewModel.attraction.setManufacturer(this.viewModel.manufacturer);
            this.viewModel.attraction.setStatus(this.viewModel.status);
            this.viewModel.attraction.setUntracktedRideCount(this.viewModel.untrackedRideCount);

            Log.d(Constants.LOG_TAG, String.format("CreateOrEditAttractionActivity.createAttraction:: created %s", this.viewModel.attraction.getFullName()));

            success = true;
        }

        Log.d(Constants.LOG_TAG, String.format("CreateOrEditAttractionActivity.createAttraction:: created successfuly [%S]", success));
        return success;
    }

    private void returnResult(int resultCode)
    {
        Log.i(Constants.LOG_TAG, String.format("CreateOrEditAttractionActivity.returnResult:: resultCode[%d]", resultCode));

        Intent intent = new Intent();

        if(resultCode == RESULT_OK)
        {
            Log.i(Constants.LOG_TAG, String.format("CreateOrEditAttractionActivity.returnResult:: returning %s", this.viewModel.attraction));
            intent.putExtra(Constants.EXTRA_ELEMENT_UUID, this.viewModel.attraction.getUuid().toString());
        }
        else
        {
            Log.i(Constants.LOG_TAG, "CreateOrEditAttractionActivity.returnResult:: no changes - returning no element");
        }

        setResult(resultCode, intent);
        Log.i(Constants.LOG_TAG, Constants.LOG_DIVIDER_FINISH + this.getClass().getSimpleName());
        finish();
    }
}