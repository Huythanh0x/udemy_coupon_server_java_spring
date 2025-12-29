/**
 * TypeScript type definitions for Udemy API responses
 * Based on analysis of Udemy API endpoints
 */

// ============================================
// Common Types
// ============================================

export interface UdemyInstructor {
  _class: 'user'
  title: string
  name: string
  display_name: string
  job_title: string
  image_50x50: string
  image_100x100: string
  initials: string
  url: string
}

export interface UdemyLocale {
  _class: 'locale'
  locale: string
  title: string
  english_title: string
  simple_english_title: string
}

export interface UdemyCategory {
  id: number
  title: string
  title_cleaned: string
  url: string
  icon_class: string
  type: 'category'
  channel_id: string | null
  _class: 'course_category'
}

export interface UdemySubcategory {
  id: number
  title: string
  title_cleaned: string
  url: string
  icon_class: string
  type: 'subcategory'
  channel_id: string | null
  _class: 'course_subcategory'
}

export interface UdemyPrice {
  amount: number
  currency: string
  price_string: string
  currency_symbol: string
}

// ============================================
// 1. Course Basic Information Response
// ============================================

export interface UdemyCourseResponse {
  _class: 'course'
  id: number
  title: string
  visible_instructors: UdemyInstructor[]
  locale: UdemyLocale
  num_subscribers: number
  avg_rating_recent: number
  primary_category: UdemyCategory
  primary_subcategory: UdemySubcategory | null
  estimated_content_length: number // in minutes
  context_info: {
    category: {
      id: number
      title: string
      url: string
      tracking_object_type: string
    }
    subcategory: {
      id: number
      title: string
      url: string
      tracking_object_type: string
    } | null
    label: {
      id: number
      display_name: string
      title: string
      topic_channel_url: string
      url: string
      tracking_object_type: string
    } | null
  }
}

// ============================================
// 2. Reviews Response
// ============================================

export interface UdemyReviewsResponse {
  count: number
  next: string | null
  previous: string | null
  results: UdemyReview[]
}

export interface UdemyReview {
  _class: 'course_review'
  id: number
  content: string
  content_html: string
  rating: number
  created: string // ISO 8601 date
  modified: string
  user_modified: string
  created_formatted_with_time_since: string // e.g., "4 days ago"
  user: UdemyReviewUser
  response: UdemyReviewResponse | null
}

export interface UdemyReviewUser {
  _class: 'user'
  title: string
  name: string
  display_name: string
  image_50x50: string
  initials: string
  tracking_id: string
  public_display_name: string
}

export interface UdemyReviewResponse {
  _class: 'course_review_response'
  id: number
  content: string
  content_html: string
  created: string
  modified: string
  created_formatted_with_time_since: string
  user: UdemyReviewUser
}

// ============================================
// 3. Course Landing Components Response
// ============================================

export interface UdemyCourseLandingResponse {
  slider_menu?: {
    data: {
      title: string
      badge_family: string | null
      is_free_seo_exp: boolean
      rating: number
      num_reviews: number
      num_students: number
      is_coding_exercises_badge_eligible: boolean
      show_money_back_guarantee: boolean
    }
  }
  buy_button?: {
    button: {
      add_to_cart_redirect_url: string
      base_express_checkout: string
      enrollment_disabled: boolean
      event_type: string
      icon: string | null
      is_free_with_discount: boolean
      require_popup: boolean
      text: string
      buy_url: string
      preview_url: string
      payment_data: {
        buyableId: number
        buyableType: string
        discountInfo: {
          code: string
        }
        purchasePrice: UdemyPrice
      }
      size: string
      style: string
      is_paid: boolean
      is_enabled: boolean
    }
  }
  price_text?: {
    data: {
      is_valid_student: boolean
      purchase_date: string | null
      enrolled_via_subscription_date: string | null
      is_in_subscription: boolean
      show_discount_info: boolean
      pricing_result: UdemyPricingResult
      course_id: number
      list_price: UdemyPrice
      is_organization_only: boolean
      is_free_for_organization: boolean
      show_percent_discount: boolean
      is_enabled: boolean
    }
  }
  incentives?: {
    is_free_seo_exp: boolean
    video_content_length: string
    audio_content_length: string
    num_articles: number
    num_quizzes: number
    num_practice_tests: number
    num_coding_exercises: number
    num_additional_resources: number
    has_lifetime_access: boolean
    devices_access: string
    has_assignments: boolean
    has_certificate: boolean
    num_cpe_credits: number
    placement: string
    reorder_incentives: boolean
    show_incentives_on_tablet: boolean
    show_quizzes: boolean
    move_lifetime_access_to_purchase_section: boolean
    has_closed_captions: boolean
    has_audio_description: boolean
  }
  curriculum_context?: {
    data: {
      sections: UdemyCurriculumSection[]
      estimated_content_length_text: string
      estimated_content_length_in_seconds: number
      is_for_practice_test_course: boolean
      num_of_published_lectures: number
      is_limited_consumption_trial: boolean
      url: string
      tracking_id: string
    }
  }
  discount_expiration?: {
    data: {
      discount_deadline_text: string
      is_enabled: boolean
    }
  }
  redeem_coupon?: {
    discount_attempts: Array<{
      code: string
      status: string
      details: any | null
      amount: any | null
      source_type: string
    }>
    has_already_purchased: boolean
  }
  money_back_guarantee?: {
    is_enabled: boolean
    cta_refund_policy: string | null
  }
  lifetime_access_context?: {
    hasLifetimeAccess: boolean
  }
  gift_this_course?: {
    gift_this_course_link: string
    round: number | null
  }
  buy_for_team?: {
    data: {
      ufb_demo_link: string
      ufb_copy_context: {
        title: string
        content: string
      }
      ufb_button_copy: string
      buy_for_team_ref: string
      is_enabled: boolean
      isOnsiteRequestDemo: boolean
    }
  }
  sidebar_container?: {
    componentProps: {
      addToCart: {
        buyables: Array<{
          buyable_object_type: string
          id: number
          image_100x100: string
          title: string
          visible_instructors: Array<{ title: string }>
        }>
        onAddRedirectUrl: string
        is_enabled: boolean
      }
      introductionAsset: {
        has_video_asset: boolean
        course_preview_path: string
        images: {
          image_240x135: string
          image_480x270: string
          image_750x422: string
        }
      }
      purchaseSection: {
        is_course_paid: boolean
        has_subscription_offerings: boolean
      }
      purchaseInfo: {
        isValidStudent: boolean
        purchaseDate: string | null
      }
      moneyBackGuarantee: {
        is_enabled: boolean
      }
    }
  }
}

export interface UdemyPricingResult {
  price_serve_tracking_id: string
  price: UdemyPrice
  list_price: UdemyPrice
  saving_price: UdemyPrice
  has_discount_saving: boolean
  discount_percent: number
  discount_percent_for_display: number
  buyable: {
    id: number
    type: string
  }
  campaign: {
    code: string
    end_time: string
    is_instructor_created: boolean
    is_public: boolean
    start_time: string
    campaign_type: string
    uses_remaining: number
    maximum_uses: number
    show_code: boolean
  }
  code: string
  is_public: boolean
}

export interface UdemyCurriculumSection {
  content_length_text: string
  content_length: number // in seconds
  index: number
  items: UdemyCurriculumItem[]
  lecture_count: number
  title: string
}

export interface UdemyCurriculumItem {
  can_be_previewed: boolean
  content_summary: string
  description: string
  id: number
  icon_class: string
  is_coding_exercise: boolean
  is_practice_test: boolean
  has_linked_workspace: boolean
  has_linked_lab: boolean
  landing_page_url: string | null
  video_asset_id: number | null
  preview_url: string
  learn_url: string
  title: string
  object_index: number
  item_type: 'lecture' | 'quiz' | 'assignment' | 'practice_test'
}

// ============================================
// 4. Discovery/Related Courses Response
// ============================================

export interface UdemyDiscoveryResponse {
  units: UdemyDiscoveryUnit[]
  custom_units: any[]
  more_units_available: boolean
  last_unit_index: number
  context: {
    _class: 'discovery_context'
  }
}

export interface UdemyDiscoveryUnit {
  title: string
  raw_title: string
  source_objects: Array<{
    type: string
    id: number
    title: string
    description: string
    url: string
  }>
  item_type: string
  items: UdemyDiscoveryCourse[]
  secondary_items: any[]
  remaining_item_count: number
  url: string
  type: string
  recommendation_params: {
    fl: string
    sos: string
    u: number
    fft: string | null
    is_content_rankable: boolean
    context: string
    ref_tracking_id: string
    course_id: number
    apply_campaign_filter: boolean
    discovery_configuration_id: number
    timestamp: number
    ranking_index: number
  }
  url_title: string | null
  score: number
  available_filters: Record<string, any>
  aggregations: any | null
  course_labels: any | null
  sort_options: Record<string, any>
  pagination: Record<string, any>
  view_type: string
  member_of: any | null
  tracking_id: string
  has_courses_in_ufb: boolean
  description: string
  subcategory_slug: string
  fbt_discount_savings_percent: number | null
  fbt_go_direct_to_cart: boolean
}

export interface UdemyDiscoveryCourse {
  _class: 'course'
  id: number
  title: string
  url: string
  is_paid: boolean
  visible_instructors: UdemyInstructor[]
  image_125_H: string
  image_240x135: string
  image_480x270: string
  image_750x422: string
  published_title: string
  tracking_id: string
  locale: UdemyLocale
  headline: string
  num_subscribers: number
  caption_locales: UdemyLocale[]
  avg_rating: number
  avg_rating_recent: number
  rating: number
  num_reviews: number
  is_wishlisted: boolean
  num_published_lectures: number
  num_published_practice_tests: number
  instructional_level: string
  instructional_level_simple: string
  content_length_practice_test_questions: number
  is_user_subscribed: boolean
  buyable_object_type: string
  published_time: string
  objectives_summary: string[]
  is_recently_published: boolean
  last_update_date: string
  preview_url: string
  learn_url: string
  content_info: string
  content_info_short: string
  context_info: {
    category: {
      id: number
      title: string
      url: string
      tracking_object_type: string
    }
    subcategory: {
      id: number
      title: string
      url: string
      tracking_object_type: string
    } | null
    label: {
      id: number
      display_name: string
      title: string
      topic_channel_url: string
      url: string
      tracking_object_type: string
    } | null
  }
}

