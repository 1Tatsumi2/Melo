using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace MusicApp.Models
{
	public class AlbumDetailViewModel
	{
		public int Ma_Album { get; set; }
		public string Ten_Album { get; set; }
		public string HinhAnh { get; set; }
		public System.DateTime Ngay_Phat_Hanh { get; set; }

		// Thông tin về ca sĩ của album
		public int Ma_Ca_Si { get; set; }
		public string Ten_Ca_Si { get; set; }
		public string HinhAnhCaSi { get; set; }
		public int SoLuongBaiHat { get; set; } // Số lượng bài hát
		public System.TimeSpan ThoiLuongDauTien { get; set; } // Thời lượng bài hát đầu tiên


		// Danh sách các bài hát trong album
		public List<SongViewModel> Songs { get; set; } = new List<SongViewModel>();
	}

}