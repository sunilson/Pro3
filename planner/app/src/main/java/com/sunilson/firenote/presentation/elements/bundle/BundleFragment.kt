package com.sunilson.firenote.presentation.elements.bundle

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.view.animation.OvershootInterpolator
import com.sunilson.firenote.R
import com.sunilson.firenote.presentation.elements.BaseElementPresenterContract
import com.sunilson.firenote.presentation.elements.elementList.ElementRecyclerAdapter
import com.sunilson.firenote.presentation.elements.elementList.ElementRecyclerAdapterFactory
import com.sunilson.firenote.presentation.homepage.adapters.SortingListArrayAdapter
import com.sunilson.firenote.presentation.shared.base.BaseFragment
import com.sunilson.firenote.presentation.shared.dialogs.elementDialog.ElementDialog
import com.sunilson.firenote.presentation.shared.interfaces.HasElementList
import com.sunilson.firenote.presentation.shared.singletons.LocalSettingsManager
import com.sunilson.firenote.presentation.shared.typeBundle
import com.sunilson.firenote.presentation.shared.typeChecklist
import com.sunilson.firenote.presentation.shared.typeNote
import jp.wasabeef.recyclerview.animators.ScaleInAnimator
import kotlinx.android.synthetic.main.base_element_activity.*
import kotlinx.android.synthetic.main.fragment_bundle.view.*
import javax.inject.Inject

class BundleFragment : BaseFragment(), BundlePresenterContract.View, HasElementList {

    @Inject
    lateinit var elementRecyclerAdapterFactory: ElementRecyclerAdapterFactory

    @Inject
    lateinit var bundlePresenter: BundlePresenter

    @Inject
    lateinit var localSettingsManager: LocalSettingsManager

    @Inject
    lateinit var sortingListArrayAdapter: SortingListArrayAdapter

    override lateinit var adapter: ElementRecyclerAdapter

    override val elementActivity: BaseElementPresenterContract.View?
        get() = activity as? BaseElementPresenterContract.View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_bundle, container, false)

        view.bundleList.setHasFixedSize(true)
        view.bundleList.itemAnimator = ScaleInAnimator(OvershootInterpolator(1f))
        view.bundleList.itemAnimator.addDuration = 300
        view.bundleList.layoutManager = LinearLayoutManager(context)
        adapter = elementRecyclerAdapterFactory.create(View.OnClickListener { view -> }, View.OnLongClickListener { _ -> true }, { id, _ ->
            bundlePresenter.deleteBundleElement(id)
        }, view.bundleList)
        view.bundleList.adapter = adapter

        view.sorting_bar.localSettingsManager = localSettingsManager
        view.sorting_bar.sortingListArrayAdapter = sortingListArrayAdapter
        view.sorting_bar.sortingMethodChangedListener = {
            adapter.checkOrderAndVisibility()
        }

        /*
        view.swipeContainerChecklist.setOnRefreshListener {
            checklistRecyclerAdapter.clear()
            checklistPresenter.refreshChecklistElements()
            Handler().postDelayed({
                view.swipeContainerChecklist.isRefreshing = false
            }, 200)
        }
        */

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity?.fab_add_note?.setOnClickListener { openElementDialog(typeNote) }
        activity?.fab_add_checklist?.setOnClickListener { openElementDialog(typeChecklist) }
        activity?.fab_add_bundle?.setOnClickListener { openElementDialog(typeBundle) }
    }

    private fun openElementDialog(elementType: String) {
        ElementDialog.newInstance(getString(R.string.add_Element_Title), elementType, parent = element!!.elementID).show(fragmentManager, "dialog")
        activity?.fab2?.collapse()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_bundle, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
        }
        return super.onOptionsItemSelected(item)
    }

    override fun titleEditToggled(active: Boolean) {
    }

    companion object {
        fun newInstance(): BundleFragment {
            return BundleFragment()
        }
    }

}